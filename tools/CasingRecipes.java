import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * Writes the recipes that make empty casings, all IE data, no code:
 *   - metal press: a brass rod (forge:rods/brass) pressed in a caliber's casing mold gives that caliber's casings;
 *     the yield goes by casing size (CALIBERS below)
 *   - Engineer's Workbench: each casing mold from 3 steel plates and a wire cutter, like IE's own molds, on our own
 *     "Casing Molds" blueprint (category casing_molds)
 *   - crafting: the Casing Molds blueprint itself, IE's Molds blueprint pattern with a brass plate on top
 *
 *   java tools/CasingRecipes.java        (from the repo root)
 */
public class CasingRecipes {
    static final String MOD = "immersive_munitions";
    static final Path RECIPES = Path.of("src/main/resources/data/" + MOD + "/recipes");
    static final int PRESS_ENERGY = 2400;    // IE's own press recipes

    // Casings per brass rod, by casing size: small pistol 4, big pistol 3, rifle and shotgun 2, heavy 1
    static final Map<String, Integer> CALIBERS = new LinkedHashMap<>();
    static {
        for (String c : new String[]{"9mm", "45acp", "357mag", "762x25", "22wmr", "57x28", "46x30"}) CALIBERS.put(c, 4);
        for (String c : new String[]{"50ae", "500mag"}) CALIBERS.put(c, 3);
        for (String c : new String[]{"556x45", "545x39", "762x39", "300blk", "58x42", "308", "762x54", "68x51fury",
                "792x57", "30_06", "45_70", "12g"}) CALIBERS.put(c, 2);
        for (String c : new String[]{"338", "50bmg", "40mm"}) CALIBERS.put(c, 1);
    }

    public static void main(String[] args) throws IOException {
        int n = 0;
        for (Map.Entry<String, Integer> e : CALIBERS.entrySet()) {
            String c = e.getKey();
            write("metal_press/casing_" + c, """
                    {
                      "type": "immersiveengineering:metal_press",
                      "input": {"tag": "forge:rods/brass"},
                      "mold": "%s:mold_casing_%s",
                      "result": {"item": "%s:empty_casing_%s", "count": %d},
                      "energy": %d
                    }
                    """.formatted(MOD, c, MOD, c, e.getValue(), PRESS_ENERGY));
            write("blueprint/mold_casing_" + c, """
                    {
                      "type": "immersiveengineering:blueprint",
                      "category": "casing_molds",
                      "inputs": [
                        {"base_ingredient": {"tag": "forge:plates/steel"}, "count": 3},
                        {"item": "immersiveengineering:wirecutter"}
                      ],
                      "result": {"item": "%s:mold_casing_%s"}
                    }
                    """.formatted(MOD, c));
            n += 2;
        }
        write("crafting/blueprint_casing_molds", """
                {
                  "type": "minecraft:crafting_shaped",
                  "category": "misc",
                  "key": {
                    "B": {"tag": "forge:plates/brass"},
                    "d": {"tag": "forge:dyes/blue"},
                    "p": {"item": "minecraft:paper"}
                  },
                  "pattern": [" B ", "ddd", "ppp"],
                  "result": {"item": "immersiveengineering:blueprint", "nbt": {"blueprint": "casing_molds"}},
                  "show_notification": true
                }
                """);
        System.out.println("wrote " + (n + 1) + " recipes for " + CALIBERS.size() + " calibers");
    }

    static void write(String name, String json) throws IOException {
        Path file = RECIPES.resolve(name + ".json");
        Files.createDirectories(file.getParent());
        Files.writeString(file, json, StandardCharsets.UTF_8);
    }
}
