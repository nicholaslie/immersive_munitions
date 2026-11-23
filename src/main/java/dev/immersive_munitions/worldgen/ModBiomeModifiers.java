package dev.immersive_munitions.worldgen;

import com.mojang.serialization.Codec;
import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBiomeModifiers {
    public static final DeferredRegister<Codec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, ImmersiveMunitions.MODID);

    public static final RegistryObject<Codec<AddFeaturesUnlessModLoaded>> ADD_FEATURES_UNLESS_MOD_LOADED =
            SERIALIZERS.register("add_features_unless_mod_loaded", () -> AddFeaturesUnlessModLoaded.CODEC);
    private ModBiomeModifiers() {}
}
