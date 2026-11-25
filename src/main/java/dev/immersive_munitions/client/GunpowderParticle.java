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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** A grain of gunpowder poured by the Ammunition Assembler: a dark speck that falls into the casing and vanishes. */
public class GunpowderParticle extends TextureSheetParticle {
    GunpowderParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
        super(level, x, y, z);
        xd = vx;
        yd = vy;
        zd = vz;
        friction = 1;
        gravity = 0.5f;
        hasPhysics = false;
        lifetime = 3 + random.nextInt(2);
        quadSize = 0.032f + random.nextFloat() * 0.01f;
        float shade = 0.28f + random.nextFloat() * 0.17f;
        setColor(shade, shade, shade);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Mod.EventBusSubscriber(modid = ImmersiveMunitions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
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
