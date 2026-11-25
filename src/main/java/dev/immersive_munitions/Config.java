package dev.immersive_munitions;

import net.minecraftforge.common.ForgeConfigSpec;

/** Common config: whether zinc ore generates, and whether it steps aside for Immersive Geology. */
public final class Config {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ZINC_WORLDGEN;
    public static final ForgeConfigSpec.BooleanValue YIELD_TO_GEOLOGY;

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
        builder.pop();

        SPEC = builder.build();
    }

    private Config() {}
}
