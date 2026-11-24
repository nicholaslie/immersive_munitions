package dev.immersive_munitions.registry;

import dev.immersive_munitions.ImmersiveMunitions;
import dev.immersive_munitions.multiblock.AmmoAssemblerMenu;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ImmersiveMunitions.MODID);

    // Opened from the Ammunition Assembler's front screens; the client builds its side with no extra data, like IE's menus
    public static final RegistryObject<MenuType<AmmoAssemblerMenu>> AMMO_ASSEMBLER = MENUS.register("ammo_assembler",
            () -> new MenuType<>(AmmoAssemblerMenu::makeClient, FeatureFlagSet.of()));

    private ModMenus() {}
}
