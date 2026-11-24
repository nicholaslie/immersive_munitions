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

/**
 * A wisp of the small smoke poof that comes out of the Ammunition Assembler's drop tube with each pour: it slows
 * quickly, breaks up (vanilla's poof sprites, played by age) and fades.
 */
public class PowderSmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    PowderSmokeParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        xd = vx;
        yd = vy;
        zd = vz;
        friction = 0.7f;
        gravity = 0;
        hasPhysics = false;
        lifetime = 5 + random.nextInt(3);
        quadSize = 0.022f + random.nextFloat() * 0.012f;
        float shade = 0.55f + random.nextFloat() * 0.15f;
        setColor(shade, shade, shade * 0.95f);
        alpha = 0.6f;
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        setSpriteFromAge(sprites);
        alpha = 0.6f * (1 - age / (float) lifetime);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Mod.EventBusSubscriber(modid = ImmersiveMunitions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
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
