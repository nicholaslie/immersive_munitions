package dev.immersive_munitions.multiblock;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInWorld;
import dev.immersive_munitions.crafting.AmmoAssemblyRecipe;
import dev.immersive_munitions.multiblock.AmmoAssemblerLogic.State;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.BiFunction;

/**
 * One casing riding the belt. It stops under the hopper until it gets its gunpowder,
 * then under the piston until it gets its projectile, then turns into the finished round.
 */
public class AssemblyProcess extends MultiblockProcessInWorld<AmmoAssemblyRecipe> {
    private boolean filled;
    private boolean pressed;

    public AssemblyProcess(AmmoAssemblyRecipe recipe, NonNullList<ItemStack> casing) {
        super(recipe, AmmoAssemblerLogic.PRESS_POINT, casing);
    }

    public AssemblyProcess(BiFunction<Level, ResourceLocation, AmmoAssemblyRecipe> getRecipe, CompoundTag nbt) {
        super(getRecipe, nbt);
        filled = nbt.getBoolean("filled");
        pressed = nbt.getBoolean("pressed");
    }

    public boolean isFilled() {
        return filled;
    }

    public boolean isPressed() {
        return pressed;
    }

    @Override
    public boolean canProcess(ProcessContextInWorld<AmmoAssemblyRecipe> context, Level level) {
        if (!super.canProcess(context, level)) return false;
        AmmoAssemblyRecipe recipe = getRecipe(level);
        if (recipe == null) return true;

        State state = (State) context;
        if (reachesFill(level) && !state.take(state.gunpowder, recipe.gunpowder, true)) return false;
        if (reachesPress(level) && !state.take(state.projectiles, recipe.projectile, true)) return false;
        return true;
    }

    @Override
    public void doProcessTick(ProcessContextInWorld<AmmoAssemblyRecipe> context, IMultiblockLevel level) {
        Level world = level.getRawLevel();
        AmmoAssemblyRecipe recipe = getRecipe(world);
        if (recipe != null) {
            State state = (State) context;
            if (reachesFill(world)) {
                if (!state.take(state.gunpowder, recipe.gunpowder, false)) return;
                filled = true;
            }
            if (reachesPress(world)) {
                if (!state.take(state.projectiles, recipe.projectile, false)) return;
                pressed = true;
            }
        }
        super.doProcessTick(context, level);
    }

    // True on the tick the casing would move onto a station it hasn't been served by yet
    private boolean reachesFill(Level level) {
        return !filled && processTick + 1 >= AmmoAssemblerLogic.FILL_POINT * getMaxTicks(level);
    }

    private boolean reachesPress(Level level) {
        return !pressed && processTick + 1 >= AmmoAssemblerLogic.PRESS_POINT * getMaxTicks(level);
    }

    @Override
    public void writeExtraDataToNBT(CompoundTag nbt) {
        super.writeExtraDataToNBT(nbt);
        nbt.putBoolean("filled", filled);
        nbt.putBoolean("pressed", pressed);
    }
}
