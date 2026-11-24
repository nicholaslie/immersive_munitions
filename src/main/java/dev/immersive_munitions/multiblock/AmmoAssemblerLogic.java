package dev.immersive_munitions.multiblock;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInWorld;
import blusunrize.immersiveengineering.common.util.DroppingMultiblockOutput;
import dev.immersive_munitions.crafting.AmmoAssemblyRecipe;
import dev.immersive_munitions.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/*
 * Positions are in blueprint space: x 0..2 along the belt (input at 0), y 0..2, z 0 = back row, z 1 = belt row.
 * IE calls the back face FRONT and the input-end face RIGHT; see the bottling machine, which has the same layout.
 */
public class AmmoAssemblerLogic implements IMultiblockLogic<AmmoAssemblerLogic.State>,
        IServerTickableComponent<AmmoAssemblerLogic.State>, IClientTickableComponent<AmmoAssemblerLogic.State> {
    public static final BlockPos REDSTONE_POS = new BlockPos(1, 0, 1);
    private static final BlockPos BELT_START = new BlockPos(0, 1, 1);
    private static final CapabilityPosition CASING_INPUT = new CapabilityPosition(0, 1, 1, RelativeBlockFace.RIGHT);
    private static final MultiblockFace OUTPUT = new MultiblockFace(3, 1, 1, RelativeBlockFace.RIGHT);
    // Roof intakes over the back row (gunpowder over the first bay, projectiles over the middle bay),
    // power plate on the back of the press bay
    private static final CapabilityPosition GUNPOWDER_INPUT = new CapabilityPosition(0, 2, 0, RelativeBlockFace.UP);
    private static final CapabilityPosition PROJECTILE_INPUT = new CapabilityPosition(1, 2, 0, RelativeBlockFace.UP);
    private static final CapabilityPosition ENERGY_INPUT = new CapabilityPosition(2, 0, 0, RelativeBlockFace.FRONT);

    // How far along the belt (0..1) each station sits: one per block, centred on it
    public static final float FILL_POINT = 1 / 6f;
    public static final float PLACE_POINT = 0.5f;
    public static final float PRESS_POINT = 5 / 6f;
    // Casings keep at least this much of a cycle between each other. With a 60-tick recipe that is one casing
    // every 7.5 ticks, slightly faster than a hopper (8 ticks per item), so a hopper feed never backs up.
    private static final float MIN_SPACING = 1 / 8f;
    // Enough room on the belt for a full cycle's worth of casings at that spacing
    private static final int MAX_CASINGS = 8;
    private static final int RESYNC_TICKS = 20;

    @Override
    public State createInitialState(IInitialMultiblockContext<State> ctx) {
        return new State(ctx);
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        boolean active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        boolean powder = !state.gunpowder.getStackInSlot(0).isEmpty();
        ItemStack projectile = state.projectiles.getStackInSlot(0);
        // The client runs the belt itself (tickClient); resync when what it relies on changes, and now and then
        // while running so its casings don't drift
        boolean resync = active != state.active || powder != state.hasGunpowder
                || !ItemStack.isSameItemSameTags(projectile, state.feederProjectile)
                || (active && ++state.ticksSinceSync >= RESYNC_TICKS);
        state.active = active;
        state.hasGunpowder = powder;
        if (!ItemStack.isSameItemSameTags(projectile, state.feederProjectile))
            state.feederProjectile = projectile.copyWithCount(1);
        if (resync) {
            state.ticksSinceSync = 0;
            ctx.requestMasterBESync();
        }
    }

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        Level level = ctx.getLevel().getRawLevel();
        for (MultiblockProcess<AmmoAssemblyRecipe, ?> process : state.processor.getQueue())
            if (process instanceof AssemblyProcess casing)
                casing.tickClient(state, level, state.active && state.additionalCanProcessCheck(process, level));
    }

    @Override
    public void onEntityCollision(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Entity entity) {
        Level level = ctx.getLevel().getRawLevel();
        if (level.isClientSide || !BELT_START.equals(posInMultiblock)) return;
        if (!(entity instanceof ItemEntity itemEntity) || !entity.isAlive()) return;

        ItemStack stack = itemEntity.getItem();
        ItemStack rest = ctx.getState().casingInput.insertItem(0, stack, false);
        if (rest.getCount() != stack.getCount()) {
            if (rest.isEmpty()) entity.discard();
            else itemEntity.setItem(rest);
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        State state = ctx.getState();
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (CASING_INPUT.equals(position)) return state.casingCap.cast(ctx);
            if (GUNPOWDER_INPUT.equalsOrNullFace(position)) return state.gunpowderCap.cast(ctx);
            if (PROJECTILE_INPUT.equalsOrNullFace(position)) return state.projectileCap.cast(ctx);
        } else if (cap == ForgeCapabilities.ENERGY && ENERGY_INPUT.equalsOrNullFace(position)) {
            return state.energyCap.cast(ctx);
        }
        return LazyOptional.empty();
    }

    @Override
    public void dropExtraItems(State state, Consumer<ItemStack> drop) {
        drop.accept(state.gunpowder.getStackInSlot(0).copy());
        drop.accept(state.projectiles.getStackInSlot(0).copy());
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType) {
        return pos -> pos.getY() == 1 && pos.getZ() == 1
                ? Shapes.box(0, 0, 0, 1, 0.125, 1)   // the belt, so items can land on it
                : Shapes.block();
    }

    public static class State implements IMultiblockState, ProcessContextInWorld<AmmoAssemblyRecipe> {
        public final RSState rsState = RSState.enabledByDefault();
        public final MultiblockProcessor<AmmoAssemblyRecipe, ProcessContextInWorld<AmmoAssemblyRecipe>> processor;
        final ItemStackHandler gunpowder;
        final ItemStackHandler projectiles;
        final IItemHandler casingInput;
        private final AveragingEnergyStorage energy = new AveragingEnergyStorage(16000);
        private final DroppingMultiblockOutput output;
        private boolean active;
        // Synced so the client can run the belt and show the feeder's projectile
        boolean hasGunpowder;
        // One of what's in the projectile hopper (empty if none): the round the feeder's jaws hold
        ItemStack feederProjectile = ItemStack.EMPTY;
        private int ticksSinceSync;
        // Client only: casings filled so far, which turns the powder handwheel
        int clientFills;

        private final StoredCapability<IItemHandler> casingCap;
        private final StoredCapability<IItemHandler> gunpowderCap;
        private final StoredCapability<IItemHandler> projectileCap;
        private final StoredCapability<IEnergyStorage> energyCap = new StoredCapability<>(energy);

        State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            processor = new MultiblockProcessor<>(MAX_CASINGS, maxTicks -> MIN_SPACING, MAX_CASINGS, markDirty, ctx.getSyncRunnable(),
                    AmmoAssemblyRecipe.RECIPES::getById);
            gunpowder = slot(markDirty, stack -> stack.is(Tags.Items.GUNPOWDER));
            projectiles = slot(markDirty, stack -> stack.is(ModTags.PROJECTILES));
            casingInput = new CasingInput(ctx.levelSupplier(), this);
            output = new DroppingMultiblockOutput(OUTPUT, ctx);

            casingCap = new StoredCapability<>(casingInput);
            gunpowderCap = new StoredCapability<>(gunpowder);
            projectileCap = new StoredCapability<>(projectiles);
        }

        private static ItemStackHandler slot(Runnable markDirty, Predicate<ItemStack> valid) {
            return new ItemStackHandler(1) {
                @Override
                public boolean isItemValid(int slot, ItemStack stack) {
                    return valid.test(stack);
                }

                @Override
                protected void onContentsChanged(int slot) {
                    markDirty.run();
                }
            };
        }

        /** Takes the gunpowder (or projectile) for a recipe out of its hopper. Returns false if there isn't enough. */
        boolean take(ItemStackHandler from, IngredientWithSize what, boolean simulate) {
            ItemStack stack = from.getStackInSlot(0);
            if (!what.test(stack) || stack.getCount() < what.getCount()) return false;
            if (!simulate) from.extractItem(0, what.getCount(), false);
            return true;
        }

        // Stops a casing from catching up to the one in front of it while that one waits at a station
        @Override
        public boolean additionalCanProcessCheck(MultiblockProcess<AmmoAssemblyRecipe, ?> process, Level level) {
            List<? extends MultiblockProcess<AmmoAssemblyRecipe, ?>> queue = processor.getQueue();
            int index = queue.indexOf(process);
            if (index <= 0) return true;
            MultiblockProcess<AmmoAssemblyRecipe, ?> ahead = queue.get(index - 1);
            int gap = ahead.processTick - process.processTick;
            return gap > MIN_SPACING * process.getMaxTicks(level);
        }

        @Override
        public AveragingEnergyStorage getEnergy() {
            return energy;
        }

        @Override
        public void doProcessOutput(ItemStack result, IMultiblockLevel level) {
            output.insertOrDrop(result, level);
        }

        public boolean isActive() {
            return active;
        }

        public boolean hasProjectiles() {
            return !feederProjectile.isEmpty();
        }

        public ItemStack feederProjectile() {
            return feederProjectile;
        }

        public int clientFills() {
            return clientFills;
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("energy", energy.serializeNBT());
            nbt.put("gunpowder", gunpowder.serializeNBT());
            nbt.put("projectiles", projectiles.serializeNBT());
            nbt.put("processor", processor.toNBT());
        }

        @Override
        public void readSaveNBT(CompoundTag nbt) {
            energy.deserializeNBT(nbt.get("energy"));
            gunpowder.deserializeNBT(nbt.getCompound("gunpowder"));
            projectiles.deserializeNBT(nbt.getCompound("projectiles"));
            processor.fromNBT(nbt.get("processor"), AssemblyProcess::new);
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt) {
            nbt.put("processor", processor.toNBT());
            nbt.putBoolean("active", active);
            nbt.putBoolean("powder", hasGunpowder);
            if (!feederProjectile.isEmpty()) nbt.put("projectile", feederProjectile.save(new CompoundTag()));
        }

        @Override
        public void readSyncNBT(CompoundTag nbt) {
            processor.fromNBT(nbt.get("processor"), AssemblyProcess::new);
            active = nbt.getBoolean("active");
            hasGunpowder = nbt.getBoolean("powder");
            feederProjectile = nbt.contains("projectile") ? ItemStack.of(nbt.getCompound("projectile")) : ItemStack.EMPTY;
        }
    }

    /** Casings put onto the start of the belt by hand, conveyor or hopper. One casing per process. */
    private record CasingInput(Supplier<Level> level, State state) implements IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            Level world = level.get();
            if (world == null || stack.isEmpty()) return stack;
            AmmoAssemblyRecipe recipe = AmmoAssemblyRecipe.findForCasing(world, stack);
            if (recipe == null) return stack;

            ItemStack one = stack.copyWithCount(1);
            AssemblyProcess process = new AssemblyProcess(recipe, NonNullList.of(ItemStack.EMPTY, one));
            if (!state.processor.addProcessToQueue(process, world, simulate)) return stack;
            return stack.copyWithCount(stack.getCount() - 1);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }
    }
}
