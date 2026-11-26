package dev.immersive_munitions.registry;

import dev.immersive_munitions.ImmersiveMunitions;
import dev.immersive_munitions.multiblock.AmmoAssemblerMenu;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Menu (GUI) types. */
public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ImmersiveMunitions.MODID);

    // Opened from the Ammunition Assembler's front screens; the client builds its side with no extra data, like IE's menus
    public static final DeferredHolder<MenuType<?>, MenuType<AmmoAssemblerMenu>> AMMO_ASSEMBLER = MENUS.register("ammo_assembler",
            () -> new MenuType<>(AmmoAssemblerMenu::makeClient, FeatureFlagSet.of()));

    private ModMenus() {}
}
