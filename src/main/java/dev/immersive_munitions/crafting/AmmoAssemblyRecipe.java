package dev.immersive_munitions.crafting;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.TagOutputList;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import dev.immersive_munitions.registry.ModRecipes;
import dev.immersive_munitions.tacz.TaczAmmoStacks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Ammo assembler recipe: one casing, filled with gunpowder, then pressed together with a projectile. */
public class AmmoAssemblyRecipe extends MultiblockRecipe {
    public static final CachedRecipeList<AmmoAssemblyRecipe> RECIPES = new CachedRecipeList<>(ModRecipes.AMMO_ASSEMBLY);
    // IE scales its machines' time and energy by config; the assembler has no such config, so it runs as written
    private static final RecipeMultiplier AS_WRITTEN = new RecipeMultiplier(() -> 1, () -> 1);

    public final IngredientWithSize casing;
    public final IngredientWithSize projectile;
    public final IngredientWithSize gunpowder;
    public final String ammoId;
    public final int count;
    // Don't draw the projectile on the casing between feeder and press (shotshells: the shot goes inside the hull)
    public final boolean hideProjectile;

    public AmmoAssemblyRecipe(IngredientWithSize casing, IngredientWithSize projectile, IngredientWithSize gunpowder,
                              String ammoId, int count, int time, int energy, boolean hideProjectile) {
        super(new TagOutput(TaczAmmoStacks.create(ammoId, count)), ModRecipes.AMMO_ASSEMBLY, time, energy, () -> AS_WRITTEN);
        this.casing = casing;
        this.projectile = projectile;
        this.gunpowder = gunpowder;
        this.ammoId = ammoId;
        this.count = count;
        this.hideProjectile = hideProjectile;

        setInputListWithSizes(List.of(casing, projectile, gunpowder));
        outputList = new TagOutputList(new TagOutput(TaczAmmoStacks.create(ammoId, count)));
        fluidInputList = List.of();
        fluidOutputList = List.of();
    }

    @Nullable
    public static RecipeHolder<AmmoAssemblyRecipe> findForCasing(Level level, ItemStack casingStack) {
        if (casingStack.isEmpty()) return null;
        for (RecipeHolder<AmmoAssemblyRecipe> recipe : RECIPES.getRecipes(level)) {
            if (recipe.value().casing.test(casingStack)) return recipe;
        }
        return null;
    }

    @Override
    public int getMultipleProcessTicks() {
        return 0;
    }

    @Override
    protected IERecipeSerializer<AmmoAssemblyRecipe> getIESerializer() {
        return ModRecipes.AMMO_ASSEMBLY_SERIALIZER.get();
    }
}
