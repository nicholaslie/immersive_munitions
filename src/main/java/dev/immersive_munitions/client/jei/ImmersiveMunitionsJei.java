package dev.immersive_munitions.client.jei;

import dev.immersive_munitions.ImmersiveMunitions;
import dev.immersive_munitions.Config;
import dev.immersive_munitions.compat.ArmorerCompat;
import dev.immersive_munitions.compat.MaterialCompat;
import dev.immersive_munitions.compat.RemovedRecipes;
import dev.immersive_munitions.crafting.AmmoAssemblyRecipe;
import dev.immersive_munitions.registry.ModMultiblocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/**
 * JEI support: adds the Ammunition Assembler's recipes, and hides our materials that another mod already provides
 * and the IE guns whose recipes are switched off.
 * JEI loads this only when it's installed, and nothing else in the mod uses this package, so JEI stays optional.
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
        registration.addRecipes(AmmoAssemblyCategory.TYPE, AmmoAssemblyRecipe.RECIPES.getRecipes(level).stream().map(RecipeHolder::value).toList());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModMultiblocks.AMMO_ASSEMBLER.iconStack(), AmmoAssemblyCategory.TYPE);
    }

    // hides our zinc and brass items that Create or Immersive Geology already provide (MaterialCompat), and IE's
    // guns and their ammo when their recipes are switched off (RemovedRecipes), and the Immersive Armorer parts when
    // that gun pack isn't installed (ArmorerCompat)
    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        var ingredients = runtime.getIngredientManager();
        boolean ieWeapons = Config.REMOVE_IE_WEAPONS.get(), noArmorer = !ArmorerCompat.packLoaded(true);
        List<ItemStack> hidden = ingredients.getAllIngredients(VanillaTypes.ITEM_STACK).stream()
                .filter(stack -> MaterialCompat.isHidden(stack.getItem()) || (ieWeapons && RemovedRecipes.isIeWeaponStack(stack))
                        || (noArmorer && ArmorerCompat.isArmorerStack(stack)))
                .toList();
        if (!hidden.isEmpty()) ingredients.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, hidden);
    }
}
