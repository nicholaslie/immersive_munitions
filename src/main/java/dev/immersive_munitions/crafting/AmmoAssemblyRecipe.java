package dev.immersive_munitions.crafting;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import dev.immersive_munitions.registry.ModRecipes;
import dev.immersive_munitions.tacz.TaczAmmoStacks;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;

/** Ammo assembler recipe: one casing, filled with gunpowder, then pressed together with a projectile. */
public class AmmoAssemblyRecipe extends MultiblockRecipe {
    public static final CachedRecipeList<AmmoAssemblyRecipe> RECIPES = new CachedRecipeList<>(ModRecipes.AMMO_ASSEMBLY);

    public final IngredientWithSize casing;
    public final IngredientWithSize projectile;
    public final IngredientWithSize gunpowder;
    public final String ammoId;
    public final int count;
    private final int time;
    private final int energy;

    public AmmoAssemblyRecipe(ResourceLocation id, IngredientWithSize casing, IngredientWithSize projectile,
                              IngredientWithSize gunpowder, String ammoId, int count, int time, int energy) {
        super(Lazy.of(() -> TaczAmmoStacks.create(ammoId, count)), ModRecipes.AMMO_ASSEMBLY, id);
        this.casing = casing;
        this.projectile = projectile;
        this.gunpowder = gunpowder;
        this.ammoId = ammoId;
        this.count = count;
        this.time = time;
        this.energy = energy;

        setInputListWithSizes(List.of(casing, projectile, gunpowder));
        outputList = Lazy.of(() -> NonNullList.of(ItemStack.EMPTY, TaczAmmoStacks.create(ammoId, count)));
        fluidInputList = List.<FluidTagInput>of();
        fluidOutputList = List.<FluidStack>of();
    }

    @Nullable
    public static AmmoAssemblyRecipe findForCasing(Level level, ItemStack casingStack) {
        if (casingStack.isEmpty()) return null;
        for (AmmoAssemblyRecipe recipe : RECIPES.getRecipes(level)) {
            if (recipe.casing.test(casingStack)) return recipe;
        }
        return null;
    }

    @Override
    public int getTotalProcessTime() {
        return time;
    }

    @Override
    public int getTotalProcessEnergy() {
        return energy;
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
