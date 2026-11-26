package dev.immersive_munitions;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Common config: whether zinc ore generates and whether it steps aside for Immersive Geology or Create, and which
 * other mods' recipes are switched off (TaCZ's ammo, IE's guns).
 */
public final class Config {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ZINC_WORLDGEN;
    public static final ForgeConfigSpec.BooleanValue YIELD_TO_GEOLOGY;
    public static final ForgeConfigSpec.BooleanValue YIELD_TO_CREATE;
    public static final ForgeConfigSpec.BooleanValue REMOVE_TACZ_AMMO;
    public static final ForgeConfigSpec.BooleanValue REMOVE_IE_WEAPONS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("zinc_worldgen");
        ZINC_WORLDGEN = builder
                .comment("Generate zinc ore.")
                .define("enabled", true);
        YIELD_TO_GEOLOGY = builder
                .comment("Skip zinc ore when Immersive Geology is installed, since it has its own zinc ores",
                        "(sphalerite, smithsonite). Set to false to generate both.")
                .define("disable_when_immersive_geology_present", true);
        YIELD_TO_CREATE = builder
                .comment("Skip zinc ore when Create is installed, since it has its own zinc ore. Set to false to generate both.")
                .define("disable_when_create_present", true);
        builder.pop();

        builder.comment("Recipes this mod switches off, so ammunition comes from the Ammunition Assembler.",
                "Changes apply on the next world load or /reload.").push("recipes");
        REMOVE_TACZ_AMMO = builder
                .comment("Remove the gun smith table's ammo recipes (TaCZ's default gun pack and any other gun pack).")
                .define("remove_tacz_ammo_recipes", true);
        REMOVE_IE_WEAPONS = builder
                .comment("Remove Immersive Engineering's revolver, railgun, chemthrower and both turrets, with their bullets,",
                        "casings, revolver parts, both bullet blueprints, the casing mold, and the upgrades only they use.",
                        "They're also hidden from JEI and IE's creative tab and left out of chest loot and villager trades.",
                        "Items already in a world keep working.")
                .define("remove_ie_weapons", true);
        builder.pop();

        SPEC = builder.build();
    }

    private Config() {}
}
