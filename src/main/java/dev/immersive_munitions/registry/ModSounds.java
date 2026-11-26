package dev.immersive_munitions.registry;

import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * The Ammunition Assembler's sounds. assets/immersive_munitions/sounds.json says what each one plays (for now, existing
 * Minecraft and Immersive Engineering sounds, referenced by name), so a resource pack can replace any of them.
 */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ImmersiveMunitions.MODID);

    // the machine running (a loop, faded in and out by IE's MultiblockSound)
    public static final RegistryObject<SoundEvent> ASSEMBLER_RUNNING = register("assembler.running");
    // gunpowder pouring from the nozzle into a casing
    public static final RegistryObject<SoundEvent> ASSEMBLER_POUR = register("assembler.pour");
    // the feed die's fingers letting a projectile go onto a casing
    public static final RegistryObject<SoundEvent> ASSEMBLER_PLACE = register("assembler.place");
    // the press seating a projectile
    public static final RegistryObject<SoundEvent> ASSEMBLER_PRESS = register("assembler.press");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(ImmersiveMunitions.MODID, name)));
    }

    private ModSounds() {}
}
