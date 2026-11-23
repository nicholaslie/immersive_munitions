package dev.immersive_munitions.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.immersive_munitions.Config;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.fml.ModList;

/**
 * forge:add_features, except it does nothing when the given mod is loaded (if the config allows that).
 * Biome modifiers can't have load conditions on 1.20.1, so this has to happen in code.
 */
public record AddFeaturesUnlessModLoaded(HolderSet<Biome> biomes, HolderSet<PlacedFeature> features,
                                         GenerationStep.Decoration step, String disabledIfModLoaded)
        implements BiomeModifier {

    public static final Codec<AddFeaturesUnlessModLoaded> CODEC = RecordCodecBuilder.create(i -> i.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(AddFeaturesUnlessModLoaded::biomes),
            PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(AddFeaturesUnlessModLoaded::features),
            GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(AddFeaturesUnlessModLoaded::step),
            Codec.STRING.fieldOf("disabled_if_mod_loaded").forGetter(AddFeaturesUnlessModLoaded::disabledIfModLoaded)
    ).apply(i, AddFeaturesUnlessModLoaded::new));

    private boolean enabled() {
        if (!Config.ZINC_WORLDGEN.get()) return false;
        return !(Config.YIELD_TO_GEOLOGY.get() && ModList.get().isLoaded(disabledIfModLoaded));
    }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD || !biomes.contains(biome) || !enabled()) return;
        BiomeGenerationSettingsBuilder gen = builder.getGenerationSettings();
        features.forEach(f -> gen.addFeature(step, f));
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
