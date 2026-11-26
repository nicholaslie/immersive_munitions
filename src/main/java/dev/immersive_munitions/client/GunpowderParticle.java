package dev.immersive_munitions.client;

import dev.immersive_munitions.ImmersiveMunitions;
import dev.immersive_munitions.registry.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

/** A grain of gunpowder poured by the Ammunition Assembler: a dark speck that falls into the casing and vanishes. */
public class GunpowderParticle extends TextureSheetParticle {
    GunpowderParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
        super(level, x, y, z);
        xd = vx;
        yd = vy;
        zd = vz;
        friction = 1;
        gravity = 0.3f;
        hasPhysics = false;
        lifetime = 3;   // about the fall from the nozzle to the casing mouth
        quadSize = 0.04f + random.nextFloat() * 0.015f;
        float shade = 0.28f + random.nextFloat() * 0.17f;
        setColor(shade, shade, shade);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @EventBusSubscriber(modid = ImmersiveMunitions.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double vx, double vy, double vz) {
            GunpowderParticle particle = new GunpowderParticle(level, x, y, z, vx, vy, vz);
            particle.pickSprite(sprites);
            return particle;
        }

        @SubscribeEvent
        public static void register(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.GUNPOWDER.get(), Provider::new);
        }
    }
}
