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
 * JEI support: adds the Ammunition Assembler's recipes. JEI loads this only when it's installed, and nothing else in
 * the mod uses this package, so JEI stays optional.
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
