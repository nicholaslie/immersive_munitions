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

/** A wisp of smoke from the Ammunition Assembler's powder pour: it slows, breaks up and fades. */
public class PowderSmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    PowderSmokeParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        xd = vx;
        yd = vy;
        zd = vz;
        friction = 0.9f;
        gravity = 0;
        hasPhysics = false;
        lifetime = 20 + random.nextInt(10);
        quadSize = 0.08f + random.nextFloat() * 0.05f;
        float shade = 0.5f + random.nextFloat() * 0.15f;   // mid grey, so it shows against the silver walls
        setColor(shade, shade, shade * 0.95f);
        alpha = 0.85f;
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        setSpriteFromAge(sprites);
        alpha = 0.85f * (1 - age / (float) lifetime);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @EventBusSubscriber(modid = ImmersiveMunitions.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new PowderSmokeParticle(level, x, y, z, vx, vy, vz, sprites);
        }

        @SubscribeEvent
        public static void register(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.POWDER_SMOKE.get(), Provider::new);
        }
    }
}
