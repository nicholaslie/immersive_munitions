package dev.immersive_munitions;

import dev.immersive_munitions.registry.ModBlocks;
import dev.immersive_munitions.registry.ModItems;
import dev.immersive_munitions.registry.ModTabs;
import dev.immersive_munitions.registry.AmmoComponentItems;
import dev.immersive_munitions.registry.CreativeIconItems;
import dev.immersive_munitions.worldgen.ModBiomeModifiers;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.IEventBus;

@Mod(ImmersiveMunitions.MODID)
public class ImmersiveMunitions {
    public static final String MODID = "immersive_munitions";

    public ImmersiveMunitions() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        AmmoComponentItems.ITEMS.register(bus);
        CreativeIconItems.ITEMS.register(bus);
        ModTabs.TABS.register(bus);
        ModBiomeModifiers.SERIALIZERS.register(bus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
