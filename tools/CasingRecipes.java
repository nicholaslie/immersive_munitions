import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * Writes the recipes that make empty casings, all IE data, no code:
 *   - metal press: a brass ingot (forge:ingots/brass) pressed in a caliber's casing mold gives that caliber's casings,
 *     like IE's copper ingot to casings; the yield goes by casing size (YIELD below)
 *   - Engineer's Workbench: each casing mold from 3 steel plates and a wire cutter, like IE's own molds, on one of
 *     three blueprints (BLUEPRINTS below). IE's workbench only has room for about 12 recipes per blueprint (20 dynamic
 *     slots: the blueprint, 6 inputs, the outputs; more than that shifts the slot ids and crashes the client), so the
 *     molds are split by gun type and each blueprint must stay at 12 or fewer
 *   - crafting: each blueprint itself, IE's Molds blueprint pattern with brass plates on top, in a different
 *     arrangement per blueprint
 *
 *   java tools/CasingRecipes.java        (from the repo root)
 */
public class CasingRecipes {
    static final String MOD = "immersive_munitions";
    static final Path RECIPES = Path.of("src/main/resources/data/" + MOD + "/recipes");
    static final int PRESS_ENERGY = 2400;    // IE's own press recipes
    static final int MAX_PER_BLUEPRINT = 12; // IE's workbench limit (see above)

    // Casings per brass ingot, by casing size: small pistol 4, big pistol 3, rifle and shotgun 2, heavy 1
    static final Map<String, Integer> YIELD = new LinkedHashMap<>();
    static {
        for (String c : new String[]{"9mm", "45acp", "357mag", "762x25", "22wmr", "57x28", "46x30"}) YIELD.put(c, 4);
        for (String c : new String[]{"50ae", "500mag"}) YIELD.put(c, 3);
        for (String c : new String[]{"556x45", "545x39", "762x39", "300blk", "58x42", "308", "762x54", "68x51fury",
                "792x57", "30_06", "45_70", "12g"}) YIELD.put(c, 2);
        for (String c : new String[]{"338", "50bmg", "40mm"}) YIELD.put(c, 1);
    }

    // Blueprint category -> its top crafting row (B = brass plate) and the molds on it
    record Blueprint(String category, String topRow, List<String> calibers) {}
    static final List<Blueprint> BLUEPRINTS = List.of(
            new Blueprint("pistol_casing_molds", " B ",
                    List.of("9mm", "45acp", "357mag", "762x25", "22wmr", "57x28", "46x30", "50ae", "500mag")),
            new Blueprint("rifle_casing_molds", "B B",
                    List.of("556x45", "545x39", "762x39", "300blk", "58x42", "308", "762x54", "68x51fury", "792x57",
                            "30_06", "45_70")),
            new Blueprint("heavy_casing_molds", "BBB",
                    List.of("12g", "338", "50bmg", "40mm")));

    public static void main(String[] args) throws IOException {
        int n = 0;
        for (Map.Entry<String, Integer> e : YIELD.entrySet()) {
            String c = e.getKey();
            write("metal_press/casing_" + c, """
                    {
                      "type": "immersiveengineering:metal_press",
                      "input": {"tag": "forge:ingots/brass"},
                      "mold": "%s:mold_casing_%s",
                      "result": {"item": "%s:empty_casing_%s", "count": %d},
                      "energy": %d
                    }
                    """.formatted(MOD, c, MOD, c, e.getValue(), PRESS_ENERGY));
            n++;
        }
        int molds = 0;
        for (Blueprint bp : BLUEPRINTS) {
            if (bp.calibers().size() > MAX_PER_BLUEPRINT)
                throw new IllegalStateException(bp.category() + " has " + bp.calibers().size() + " molds; IE's workbench fits " + MAX_PER_BLUEPRINT);
            for (String c : bp.calibers()) {
                write("blueprint/mold_casing_" + c, """
                        {
                          "type": "immersiveengineering:blueprint",
                          "category": "%s",
                          "inputs": [
                            {"base_ingredient": {"tag": "forge:plates/steel"}, "count": 3},
                            {"item": "immersiveengineering:wirecutter"}
                          ],
                          "result": {"item": "%s:mold_casing_%s"}
                        }
                        """.formatted(bp.category(), MOD, c));
                n++;
                molds++;
            }
            write("crafting/blueprint_" + bp.category(), """
                    {
                      "type": "minecraft:crafting_shaped",
                      "category": "misc",
                      "key": {
                        "B": {"tag": "forge:plates/brass"},
                        "d": {"tag": "forge:dyes/blue"},
                        "p": {"item": "minecraft:paper"}
                      },
                      "pattern": ["%s", "ddd", "ppp"],
                      "result": {"item": "immersiveengineering:blueprint", "nbt": {"blueprint": "%s"}},
                      "show_notification": true
                    }
                    """.formatted(bp.topRow(), bp.category()));
            n++;
        }
        if (molds != YIELD.size()) throw new IllegalStateException("blueprints cover " + molds + " molds, not " + YIELD.size());
        System.out.println("wrote " + n + " recipes for " + YIELD.size() + " calibers on " + BLUEPRINTS.size() + " blueprints");
    }

    static void write(String name, String json) throws IOException {
        Path file = RECIPES.resolve(name + ".json");
        Files.createDirectories(file.getParent());
        Files.writeString(file, json, StandardCharsets.UTF_8);
    }
}
