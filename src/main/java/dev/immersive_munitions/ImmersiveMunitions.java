package dev.immersive_munitions;

import dev.immersive_munitions.registry.AmmoComponentItems;
import dev.immersive_munitions.registry.CreativeIconItems;
import dev.immersive_munitions.registry.ModBlocks;
import dev.immersive_munitions.registry.ModItems;
import dev.immersive_munitions.registry.ModMultiblocks;
import dev.immersive_munitions.registry.ModParticles;
import dev.immersive_munitions.registry.ModRecipes;
import dev.immersive_munitions.registry.ModTabs;
import dev.immersive_munitions.worldgen.ModBiomeModifiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ImmersiveMunitions.MODID)
public class ImmersiveMunitions {
    public static final String MODID = "immersive_munitions";

    public ImmersiveMunitions(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();

        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        AmmoComponentItems.ITEMS.register(bus);
        CreativeIconItems.ITEMS.register(bus);
        ModMultiblocks.BLOCKS.register(bus);
        ModMultiblocks.ITEMS.register(bus);
        ModMultiblocks.BLOCK_ENTITIES.register(bus);
        ModRecipes.TYPES.register(bus);
        ModRecipes.SERIALIZERS.register(bus);
        ModParticles.TYPES.register(bus);
        ModTabs.TABS.register(bus);
        ModBiomeModifiers.SERIALIZERS.register(bus);

        bus.addListener(this::commonSetup);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModMultiblocks::registerStructures);
    }
}
