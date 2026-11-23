package dev.immersive_munitions;

import net.minecraftforge.common.ForgeConfigSpec;

public final class Config {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ZINC_WORLDGEN;
    public static final ForgeConfigSpec.BooleanValue YIELD_TO_GEOLOGY;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("zinc_worldgen");
        ZINC_WORLDGEN = b.comment("Master switch for this mod's own zinc ore generation.")
                .define("enabled", true);
        YIELD_TO_GEOLOGY = b.comment("If true, zinc ore generation is disabled whenever Immersive Geology is installed",
                        "(IG provides zinc via sphalerite/smithsonite). Set false to generate both.")
                .define("disable_when_immersive_geology_present", true);
        b.pop();
        SPEC = b.build();
    }
    private Config() {}
}
