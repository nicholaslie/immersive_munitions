import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * Writes the recipes that make cartridge parts, all IE data, no code:
 *   - metal press: each caliber's mold presses a brass ingot (forge:ingots/brass) into that caliber's casings, like
 *     IE's copper ingot to casings (the 40mm grenade's from aluminium, CASING_METAL); yields go by size (CASINGS)
 *   - Engineer's Workbench, mold blueprints: each mold from 3 steel plates and a wire cutter, like IE's own molds
 *   - Engineer's Workbench, projectile blueprints: each caliber's projectiles assembled like real bullets, a lead core
 *     (IE's lead wire) in a copper jacket (a copper plate); 12 Gauge shot is bare lead wire, and the 40mm grenade's
 *     warhead a steel body, copper shaped-charge liner, mechanical fuze and explosive (gunpowder). PROJECTILES below
 *   - crafting: each blueprint itself, IE's Molds blueprint pattern (blue dye, paper) with brass plates on top for the
 *     mold blueprints and copper plates for the projectile ones, in a different arrangement per gun type
 *
 * IE's workbench only has room for about 12 recipes per blueprint (20 dynamic slots: the blueprint, 6 inputs, the
 * outputs; more than that shifts the slot ids and crashes the client), so both kinds are split by gun type and each
 * blueprint must stay at 12 or fewer.
 *
 *   java tools/CasingRecipes.java        (from the repo root)
 */
public class CasingRecipes {
    static final String MOD = "immersive_munitions";
    static final Path RECIPES = Path.of("src/main/resources/data/" + MOD + "/recipes");
    static final int PRESS_ENERGY = 2400;    // IE's own press recipes
    static final int MAX_PER_BLUEPRINT = 12; // IE's workbench limit (see above)

    // Casings per brass ingot, by size: small pistol 4, big pistol 3, rifle and shotgun 2, heavy 1
    // Casings not pressed from brass: the 40mm grenade's case is aluminium, like real 40x46mm grenade cartridges
    static final Map<String, String> CASING_METAL = Map.of("40mm", "forge:ingots/aluminum");

    static final Map<String, Integer> CASINGS = new LinkedHashMap<>();
    static {
        for (String c : new String[]{"9mm", "45acp", "357mag", "762x25", "22wmr", "57x28", "46x30"}) CASINGS.put(c, 4);
        for (String c : new String[]{"50ae", "500mag"}) CASINGS.put(c, 3);
        for (String c : new String[]{"556x45", "545x39", "762x39", "300blk", "58x42", "308", "762x54", "68x51fury",
                "792x57", "30_06", "45_70", "12g"}) CASINGS.put(c, 2);
        for (String c : new String[]{"338", "50bmg", "40mm"}) CASINGS.put(c, 1);
    }

    // Projectile recipes: inputs (a tag, or "item:<id>" for one item, and a count) and how many projectiles. IE presses
    // 1 lead ingot into 2 lead wires, so the lead per projectile matches the casings per brass ingot above
    record Part(String tag, int count) {
        String json() {
            String ing = tag.startsWith("item:") ? "{\"item\": \"%s\"}".formatted(tag.substring(5)) : "{\"tag\": \"%s\"}".formatted(tag);
            return count == 1 ? ing : "{\"base_ingredient\": %s, \"count\": %d}".formatted(ing, count);
        }
    }
    record Projectile(List<Part> inputs, int count) {}
    static final Part COPPER_JACKET = new Part("forge:plates/copper", 1);
    static Projectile jacketed(int wires, int count) { return new Projectile(List.of(COPPER_JACKET, new Part("forge:wires/lead", wires)), count); }
    static final Map<String, Projectile> PROJECTILES = new LinkedHashMap<>();
    static {
        for (String c : new String[]{"9mm", "45acp", "357mag", "762x25", "22wmr", "57x28", "46x30"}) PROJECTILES.put(c, jacketed(1, 2));
        for (String c : new String[]{"50ae", "500mag"}) PROJECTILES.put(c, jacketed(2, 3));
        for (String c : new String[]{"556x45", "545x39", "762x39", "300blk", "58x42", "308", "762x54", "68x51fury",
                "792x57", "30_06", "45_70"}) PROJECTILES.put(c, jacketed(1, 1));
        for (String c : new String[]{"338", "50bmg"}) PROJECTILES.put(c, jacketed(2, 1));
        PROJECTILES.put("12g", new Projectile(List.of(new Part("forge:wires/lead", 1)), 1));       // bare lead shot
        // 40mm HEDP warhead, like the M433: a steel body (fragments), a copper shaped-charge liner, and a mechanical
        // impact fuze (IE's Iron Mechanical Component), filled with high explosive (gunpowder). The Ammunition
        // Assembler's gunpowder is the propellant in the case
        PROJECTILES.put("40mm", new Projectile(List.of(new Part("forge:plates/steel", 1), new Part("forge:plates/copper", 1),
                new Part("item:immersiveengineering:component_iron", 1), new Part("forge:gunpowder", 1)), 1));
    }

    // Gun types: the calibers on each, and the top crafting row of its blueprints (B = brass plate for the molds,
    // copper plate for the projectiles). The mold blueprint ids say "casing_molds" from before; kept so blueprints
    // already crafted keep working
    record GunType(String name, String topRow, List<String> calibers) {}
    static final List<GunType> GUN_TYPES = List.of(
            new GunType("pistol", " B ", List.of("9mm", "45acp", "357mag", "762x25", "22wmr", "57x28", "46x30", "50ae", "500mag")),
            new GunType("rifle", "B B", List.of("556x45", "545x39", "762x39", "300blk", "58x42", "308", "762x54",
                    "68x51fury", "792x57", "30_06", "45_70")),
            new GunType("heavy", "BBB", List.of("338", "50bmg")),
            new GunType("shotgun", "B  ", List.of("12g")),
            new GunType("grenade", "BB ", List.of("40mm")));

    public static void main(String[] args) throws IOException {
        int n = 0;
        // casings in the press
        for (Map.Entry<String, Integer> e : CASINGS.entrySet()) {
            String c = e.getKey();
            write("metal_press/casing_" + c, """
                    {
                      "type": "immersiveengineering:metal_press",
                      "input": {"tag": "%s"},
                      "mold": "%s:mold_casing_%s",
                      "result": {"item": "%s:empty_casing_%s", "count": %d},
                      "energy": %d
                    }
                    """.formatted(CASING_METAL.getOrDefault(c, "forge:ingots/brass"), MOD, c, MOD, c, e.getValue(), PRESS_ENERGY));
            n++;
        }
        // the blueprints, by gun type
        int molds = 0, projectiles = 0;
        for (GunType type : GUN_TYPES) {
            if (type.calibers().size() > MAX_PER_BLUEPRINT)
                throw new IllegalStateException(type.name() + " has " + type.calibers().size() + " calibers; IE's workbench fits " + MAX_PER_BLUEPRINT);
            String moldCategory = type.name() + "_casing_molds", projectileCategory = type.name() + "_projectiles";
            for (String c : type.calibers()) {
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
                        """.formatted(moldCategory, MOD, c));
                Projectile p = PROJECTILES.get(c);
                List<String> inputs = new ArrayList<>();
                for (Part part : p.inputs())
                    inputs.add(part.json());
                write("blueprint/projectile_" + c, """
                        {
                          "type": "immersiveengineering:blueprint",
                          "category": "%s",
                          "inputs": [
                            %s
                          ],
                          "result": {"item": "%s:projectile_%s", "count": %d}
                        }
                        """.formatted(projectileCategory, String.join(",\n    ", inputs), MOD, c, p.count()));
                n += 2;
                molds++;
                projectiles++;
            }
            n += blueprintRecipe(moldCategory, type.topRow(), "forge:plates/brass");
            n += blueprintRecipe(projectileCategory, type.topRow(), "forge:plates/copper");
        }
        if (molds != CASINGS.size() || projectiles != PROJECTILES.size())
            throw new IllegalStateException("blueprints cover " + molds + " molds and " + projectiles + " projectiles, not " + CASINGS.size());
        System.out.println("wrote " + n + " recipes for " + CASINGS.size() + " calibers on " + 2 * GUN_TYPES.size() + " blueprints");
    }

    static int blueprintRecipe(String category, String topRow, String plateTag) throws IOException {
        write("crafting/blueprint_" + category, """
                {
                  "type": "minecraft:crafting_shaped",
                  "category": "misc",
                  "key": {
                    "B": {"tag": "%s"},
                    "d": {"tag": "forge:dyes/blue"},
                    "p": {"item": "minecraft:paper"}
                  },
                  "pattern": ["%s", "ddd", "ppp"],
                  "result": {"item": "immersiveengineering:blueprint", "nbt": {"blueprint": "%s"}},
                  "show_notification": true
                }
                """.formatted(plateTag, topRow, category));
        return 1;
    }

    static void write(String name, String json) throws IOException {
        Path file = RECIPES.resolve(name + ".json");
        Files.createDirectories(file.getParent());
        Files.writeString(file, json, StandardCharsets.UTF_8);
    }
}
