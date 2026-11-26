package dev.immersive_munitions;

import dev.immersive_munitions.compat.MaterialCompat;
import dev.immersive_munitions.compat.RemovedWeaponLoot;
import dev.immersive_munitions.registry.AmmoComponentItems;
import dev.immersive_munitions.registry.CreativeIconItems;
import dev.immersive_munitions.registry.ModBlocks;
import dev.immersive_munitions.registry.ModItems;
import dev.immersive_munitions.registry.ModMenus;
import dev.immersive_munitions.registry.ModMultiblocks;
import dev.immersive_munitions.registry.ModParticles;
import dev.immersive_munitions.registry.ModRecipes;
import dev.immersive_munitions.registry.ModSounds;
import dev.immersive_munitions.registry.ModTabs;
import dev.immersive_munitions.worldgen.ModBiomeModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.ModContainer;

/** Mod entry point: registers everything with the mod event bus. */
@Mod(ImmersiveMunitions.MODID)
public class ImmersiveMunitions {
    public static final String MODID = "immersive_munitions";

    public ImmersiveMunitions(IEventBus bus, ModContainer container) {

        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        AmmoComponentItems.ITEMS.register(bus);
        CreativeIconItems.ITEMS.register(bus);
        ModMultiblocks.BLOCKS.register(bus);
        ModMultiblocks.ITEMS.register(bus);
        ModMultiblocks.BLOCK_ENTITIES.register(bus);
        ModMultiblocks.registerModBusListeners(bus);
        ModRecipes.TYPES.register(bus);
        ModRecipes.SERIALIZERS.register(bus);
        ModParticles.TYPES.register(bus);
        ModSounds.SOUNDS.register(bus);
        ModMenus.MENUS.register(bus);
        ModTabs.TABS.register(bus);
        ModBiomeModifiers.SERIALIZERS.register(bus);
        RemovedWeaponLoot.LOOT_MODIFIERS.register(bus);

        bus.addListener(this::commonSetup);
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModMultiblocks::registerStructures);
        event.enqueueWork(MaterialCompat::preferOtherMods);   // after IE's config has loaded
    }
}
