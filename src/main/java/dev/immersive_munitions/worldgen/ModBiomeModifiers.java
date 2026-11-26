package dev.immersive_munitions.worldgen;

import com.mojang.serialization.MapCodec;
import dev.immersive_munitions.ImmersiveMunitions;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Biome modifier serializers (zinc ore generation). */
public final class ModBiomeModifiers {
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, ImmersiveMunitions.MODID);

    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<AddFeaturesUnlessModLoaded>> ADD_FEATURES_UNLESS_MOD_LOADED =
            SERIALIZERS.register("add_features_unless_mod_loaded", () -> AddFeaturesUnlessModLoaded.CODEC);

    private ModBiomeModifiers() {}
}
