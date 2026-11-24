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
 * One casing riding the belt. It stops under the filler until it gets its gunpowder,
 * then under the feeder until it gets its projectile, then the press seats it into the finished round.
 */
public class AssemblyProcess extends MultiblockProcessInWorld<AmmoAssemblyRecipe> {
    private boolean filled;
    private boolean placed;

    public AssemblyProcess(AmmoAssemblyRecipe recipe, NonNullList<ItemStack> casing) {
        super(recipe, AmmoAssemblerLogic.PRESS_POINT, casing);
    }

    public AssemblyProcess(BiFunction<Level, ResourceLocation, AmmoAssemblyRecipe> getRecipe, CompoundTag nbt) {
        super(getRecipe, nbt);
        filled = nbt.getBoolean("filled");
        placed = nbt.getBoolean("placed");
    }

    public boolean isFilled() {
        return filled;
    }

    public boolean isPlaced() {
        return placed;
    }

    @Override
    public boolean canProcess(ProcessContextInWorld<AmmoAssemblyRecipe> context, Level level) {
        if (!super.canProcess(context, level)) return false;
        AmmoAssemblyRecipe recipe = getRecipe(level);
        if (recipe == null) return true;

        State state = (State) context;
        if (reachesFill(level) && !state.take(state.gunpowder, recipe.gunpowder, true)) return false;
        if (reachesPlace(level) && !state.take(state.projectiles, recipe.projectile, true)) return false;
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
            if (reachesPlace(world)) {
                if (!state.take(state.projectiles, recipe.projectile, false)) return;
                placed = true;
            }
        }
        super.doProcessTick(context, level);
    }

    /**
     * Client only: moves the casing one tick along, by the same rules the server uses, so the belt runs smoothly
     * between syncs. Returns whether it moved. The server's next sync corrects any drift.
     */
    boolean tickClient(State state, Level level, boolean canMove) {
        moving = false;
        int max = getMaxTicks(level);
        if (!canMove || processTick >= max) return false;
        if (reachesFill(level)) {
            if (!state.hasGunpowder) return false;
            filled = true;
            state.clientFills++;
        }
        if (reachesPlace(level)) {
            if (!state.hasProjectiles) return false;
            placed = true;
        }
        processTick++;
        moving = true;
        return true;
    }

    /** Client only: whether the casing moved on the last tick, so the renderer can interpolate. */
    public boolean isMoving() {
        return moving;
    }

    private transient boolean moving;

    // True on the tick the casing would move onto a station it hasn't been served by yet
    private boolean reachesFill(Level level) {
        return !filled && processTick + 1 >= AmmoAssemblerLogic.FILL_POINT * getMaxTicks(level);
    }

    private boolean reachesPlace(Level level) {
        return !placed && processTick + 1 >= AmmoAssemblerLogic.PLACE_POINT * getMaxTicks(level);
    }

    @Override
    public void writeExtraDataToNBT(CompoundTag nbt) {
        super.writeExtraDataToNBT(nbt);
        nbt.putBoolean("filled", filled);
        nbt.putBoolean("placed", placed);
    }
}
