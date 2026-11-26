package dev.immersive_munitions.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.immersive_munitions.Config;
import dev.immersive_munitions.compat.MaterialCompat;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

import java.util.List;

/**
 * forge:add_features, except it does nothing when one of the given mods is loaded (if the config allows that; see
 * MaterialCompat.yieldsWorldgenTo).
 * Biome modifiers can't have load conditions on 1.20.1, so this has to happen in code.
 */
public record AddFeaturesUnlessModLoaded(HolderSet<Biome> biomes, HolderSet<PlacedFeature> features,
                                         GenerationStep.Decoration step, List<String> disabledIfModsLoaded)
        implements BiomeModifier {

    public static final Codec<AddFeaturesUnlessModLoaded> CODEC = RecordCodecBuilder.create(i -> i.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(AddFeaturesUnlessModLoaded::biomes),
            PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(AddFeaturesUnlessModLoaded::features),
            GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(AddFeaturesUnlessModLoaded::step),
            Codec.STRING.listOf().fieldOf("disabled_if_mods_loaded").forGetter(AddFeaturesUnlessModLoaded::disabledIfModsLoaded)
    ).apply(i, AddFeaturesUnlessModLoaded::new));

    private boolean enabled() {
        if (!Config.ZINC_WORLDGEN.get()) return false;
        return disabledIfModsLoaded.stream().noneMatch(MaterialCompat::yieldsWorldgenTo);
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
