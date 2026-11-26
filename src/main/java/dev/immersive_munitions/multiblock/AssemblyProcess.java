package dev.immersive_munitions.multiblock;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInWorld;
import dev.immersive_munitions.crafting.AmmoAssemblyRecipe;
import dev.immersive_munitions.multiblock.AmmoAssemblerLogic.State;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

/**
 * One casing on the belt. It waits under the powder station for its gunpowder, under the feeder for its projectile,
 * and then the press turns it into the finished round.
 */
public class AssemblyProcess extends MultiblockProcessInWorld<AmmoAssemblyRecipe> {
    // How many ticks the client's belt may be off from a sync before it takes the server's position
    private static final int MAX_CLIENT_DRIFT = 2;

    // id tells casings apart across syncs; moving (client only) lets the renderer interpolate
    private final long id;
    private boolean filled;
    private boolean poured;   // client only: the powder pour for this casing has started
    private boolean placed;
    private transient boolean moving;

    public AssemblyProcess(RecipeHolder<AmmoAssemblyRecipe> recipe, NonNullList<ItemStack> casing) {
        super(recipe, AmmoAssemblerLogic.PRESS_POINT, casing);
        id = ThreadLocalRandom.current().nextLong();
    }

    public AssemblyProcess(BiFunction<Level, ResourceLocation, AmmoAssemblyRecipe> getRecipe, CompoundTag nbt, Provider provider) {
        super(getRecipe, nbt, provider);
        id = nbt.contains("id") ? nbt.getLong("id") : ThreadLocalRandom.current().nextLong();
        filled = nbt.getBoolean("filled");
        placed = nbt.getBoolean("placed");
    }

    long id() {
        return id;
    }

    public boolean isFilled() {
        return filled;
    }

    public boolean isPlaced() {
        return placed;
    }

    public boolean isMoving() {
        return moving;
    }

    // ---- Server ----

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
     * Client: true once, leadTicks before the casing reaches the powder station, so grains falling straight from the
     * nozzle land in it as it passes underneath.
     */
    boolean startsPour(Level level, int leadTicks) {
        if (poured || filled || processTick + 1 + leadTicks < AmmoAssemblerLogic.FILL_POINT * getMaxTicks(level)) return false;
        poured = true;
        return true;
    }

    // True on the tick the casing would move onto a station that hasn't served it yet
    private boolean reachesFill(Level level) {
        return !filled && processTick + 1 >= AmmoAssemblerLogic.FILL_POINT * getMaxTicks(level);
    }

    private boolean reachesPlace(Level level) {
        return !placed && processTick + 1 >= AmmoAssemblerLogic.PLACE_POINT * getMaxTicks(level);
    }

    // ---- GUI: jam message and Clear Belt ----

    /** What this casing is waiting for at its station (gunpowder or projectile), or null. */
    @Nullable
    IngredientWithSize waitingFor(State state, Level level) {
        AmmoAssemblyRecipe recipe = getRecipe(level);
        if (recipe == null) return null;
        if (reachesFill(level) && !state.take(state.gunpowder, recipe.gunpowder, true)) return recipe.gunpowder;
        if (reachesPlace(level) && !state.take(state.projectiles, recipe.projectile, true)) return recipe.projectile;
        return null;
    }

    boolean beforePress(Level level) {
        return processTick < AmmoAssemblerLogic.PRESS_POINT * getMaxTicks(level);
    }

    /** The casing plus whatever it has received so far (gunpowder, projectile). */
    List<ItemStack> parts(Level level) {
        List<ItemStack> parts = new ArrayList<>();
        if (!inputItems.isEmpty()) parts.add(inputItems.get(0).copy());
        AmmoAssemblyRecipe recipe = getRecipe(level);
        if (recipe != null) {
            if (filled) parts.add(example(recipe.gunpowder));
            if (placed) parts.add(example(recipe.projectile));
        }
        parts.removeIf(ItemStack::isEmpty);
        return parts;
    }

    private static ItemStack example(IngredientWithSize ingredient) {
        ItemStack[] stacks = ingredient.getMatchingStacks();
        return stacks.length == 0 ? ItemStack.EMPTY : stacks[0].copy();
    }

    // ---- Client belt ----

    /** Moves the casing one tick by the server's rules, so the belt runs smoothly between syncs. */
    boolean tickClient(State state, Level level, boolean canMove) {
        moving = false;
        int max = getMaxTicks(level);
        if (!canMove || processTick >= max) return false;
        if (reachesFill(level)) {
            if (!state.hasGunpowder()) return false;
            filled = true;
            state.clientFills++;
        }
        if (reachesPlace(level)) {
            if (!state.hasProjectiles()) return false;
            placed = true;
        }
        processTick++;
        moving = true;
        return true;
    }

    /**
     * On a sync: the server's numbers arrive a tick or so late, so keep the client's own position (and interpolation)
     * unless the two have drifted apart.
     */
    void keepClientProgress(AssemblyProcess client) {
        if (Math.abs(client.processTick - processTick) > MAX_CLIENT_DRIFT) return;
        processTick = client.processTick;
        filled = client.filled;
        poured = client.poured;
        placed = client.placed;
        moving = client.moving;
    }

    // ---- Saving ----

    @Override
    public void writeExtraDataToNBT(CompoundTag nbt, Provider provider) {
        super.writeExtraDataToNBT(nbt, provider);
        nbt.putLong("id", id);
        nbt.putBoolean("filled", filled);
        nbt.putBoolean("placed", placed);
    }
}
