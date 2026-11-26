package dev.immersive_munitions.registry;

import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Particles for the Ammunition Assembler's powder pour. */
public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, ImmersiveMunitions.MODID);

    // Grains of powder falling from the Ammunition Assembler's drop tube into a casing
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GUNPOWDER = TYPES.register("gunpowder", () -> new SimpleParticleType(false));
    // ...and the small smoke poof that comes out of the tube with them
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> POWDER_SMOKE = TYPES.register("powder_smoke", () -> new SimpleParticleType(false));

    private ModParticles() {}
}
