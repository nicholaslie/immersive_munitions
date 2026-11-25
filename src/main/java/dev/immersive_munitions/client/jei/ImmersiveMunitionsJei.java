package dev.immersive_munitions.client.jei;

import dev.immersive_munitions.ImmersiveMunitions;
import dev.immersive_munitions.crafting.AmmoAssemblyRecipe;
import dev.immersive_munitions.registry.ModMultiblocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * JEI support, loaded by JEI itself (through @JeiPlugin) only when JEI is installed; nothing else in the mod refers to
 * this package, so the mod runs without JEI. The press, workbench and crafting recipes already show through IE's and
 * JEI's own categories; this adds the Ammunition Assembler's.
 */
@JeiPlugin
public class ImmersiveMunitionsJei implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ImmersiveMunitions.MODID, "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new AmmoAssemblyCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        registration.addRecipes(AmmoAssemblyCategory.TYPE, List.copyOf(AmmoAssemblyRecipe.RECIPES.getRecipes(level)));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModMultiblocks.AMMO_ASSEMBLER.iconStack(), AmmoAssemblyCategory.TYPE);
    }
}
