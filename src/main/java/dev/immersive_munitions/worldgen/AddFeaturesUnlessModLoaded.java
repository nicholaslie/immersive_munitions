package dev.immersive_munitions.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.immersive_munitions.Config;
import dev.immersive_munitions.compat.MaterialCompat;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeGenerationSettingsBuilder;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

import java.util.List;

/**
 * neoforge:add_features, except it does nothing when one of the given mods is loaded (if the config allows that; see
 * MaterialCompat.yieldsWorldgenTo).
 * It reads our config, which load conditions can't, so this happens in code.
 */
public record AddFeaturesUnlessModLoaded(HolderSet<Biome> biomes, HolderSet<PlacedFeature> features,
                                         GenerationStep.Decoration step, List<String> disabledIfModsLoaded)
        implements BiomeModifier {

    public static final MapCodec<AddFeaturesUnlessModLoaded> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
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
    public MapCodec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
