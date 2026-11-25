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
import blusunrize.immersiveengineering.common.gui.IEContainerMenu.MultiblockMenuContext;
import blusunrize.immersiveengineering.common.util.DroppingMultiblockOutput;
import dev.immersive_munitions.crafting.AmmoAssemblyRecipe;
import dev.immersive_munitions.registry.ModParticles;
import dev.immersive_munitions.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The Ammunition Assembler's logic: casings ride a belt past three stations (gunpowder, projectile, press) and come
 * out as TaCZ rounds.
 * <p>
 * Positions are in blueprint space: x 0..2 along the belt (input end at 0), y 0..2, z 0 back row, z 1 belt row.
 * Like IE's bottling machine, IE calls the back face FRONT and the input end RIGHT.
 */
public class AmmoAssemblerLogic implements IMultiblockLogic<AmmoAssemblerLogic.State>,
        IServerTickableComponent<AmmoAssemblerLogic.State>, IClientTickableComponent<AmmoAssemblerLogic.State> {

    // Ports: casings in at the input end, hopper intakes on the roof above the screens, power on the roof at the back
    public static final BlockPos REDSTONE_POS = new BlockPos(1, 0, 1);
    private static final BlockPos BELT_START = new BlockPos(0, 1, 1);
    private static final CapabilityPosition CASING_INPUT = new CapabilityPosition(0, 1, 1, RelativeBlockFace.RIGHT);
    private static final MultiblockFace OUTPUT = new MultiblockFace(3, 1, 1, RelativeBlockFace.RIGHT);
    private static final CapabilityPosition GUNPOWDER_INPUT = new CapabilityPosition(0, 2, 1, RelativeBlockFace.UP);
    private static final CapabilityPosition PROJECTILE_INPUT = new CapabilityPosition(1, 2, 1, RelativeBlockFace.UP);
    private static final CapabilityPosition ENERGY_INPUT = new CapabilityPosition(2, 2, 0, RelativeBlockFace.UP);
    private static final Set<BlockPos> SCREEN_BLOCKS = Set.of(new BlockPos(0, 2, 1), new BlockPos(1, 2, 1));

    // Belt: station positions (0..1 along the belt) and casing spacing. 1/8 of a 60-tick cycle is one casing every
    // 7.5 ticks, just faster than a hopper, so a hopper feed never backs up
    public static final float FILL_POINT = 1 / 6f;
    public static final float PLACE_POINT = 0.5f;
    public static final float PRESS_POINT = 5 / 6f;
    private static final float MIN_SPACING = 1 / 8f;
    private static final int MAX_CASINGS = 8;
    public static final int ENERGY_CAPACITY = 16000;
    private static final int RESYNC_TICKS = 20, COUNT_SYNC_TICKS = 5;

    // Powder effect: the drop tube's tip (blueprint space) and the stream poured from it
    private static final Vec3 DROP_TUBE_TIP = new Vec3(0.5, 1.37, 1.5);
    private static final int STREAM_GRAINS = 30, SMOKE_WISPS = 6;
    private static final double STREAM_LENGTH = 0.12, BELT_BLOCKS = 3;

    @Override
    public State createInitialState(IInitialMultiblockContext<State> ctx) {
        return new State(ctx);
    }

    // ---- Ticking ----

    // The server runs the recipes; the client runs its own copy of the belt and is resynced when something it relies
    // on changes, and every second while running. Count-only changes wait a few ticks
    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        boolean active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        boolean powered = state.energy.getEnergyStored() > 0;
        ItemStack powder = state.gunpowder.getStackInSlot(0), projectile = state.projectiles.getStackInSlot(0);
        state.ticksSinceSync++;
        boolean counts = powder.getCount() != state.shownGunpowder.getCount()
                || projectile.getCount() != state.shownProjectiles.getCount();
        boolean resync = active != state.active || powered != state.powered
                || !ItemStack.isSameItemSameTags(powder, state.shownGunpowder)
                || !ItemStack.isSameItemSameTags(projectile, state.shownProjectiles)
                || (counts && state.ticksSinceSync >= COUNT_SYNC_TICKS)
                || (active && state.ticksSinceSync >= RESYNC_TICKS);
        state.active = active;
        state.powered = powered;
        if (resync) {
            state.shownGunpowder = powder.copy();
            state.shownProjectiles = projectile.copy();
            state.ticksSinceSync = 0;
            ctx.requestMasterBESync();
        }
    }

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        Level level = ctx.getLevel().getRawLevel();
        for (MultiblockProcess<AmmoAssemblyRecipe, ?> process : state.processor.getQueue())
            if (process instanceof AssemblyProcess casing) {
                boolean wasFilled = casing.isFilled();
                casing.tickClient(state, level, state.active && state.additionalCanProcessCheck(process, level));
                if (!wasFilled && casing.isFilled()) pourPowder(ctx.getLevel(), level, casing.getMaxTicks(level));
            }
    }

    // A quick stream of powder grains from the drop tube into the casing, plus a small puff of smoke, all moving
    // along with the belt
    private static void pourPowder(IMultiblockLevel mbLevel, Level level, int maxTicks) {
        if (maxTicks <= 0) return;
        Vec3 tube = mbLevel.toAbsolute(DROP_TUBE_TIP);
        Vec3 belt = mbLevel.toAbsolute(DROP_TUBE_TIP.add(BELT_BLOCKS / maxTicks, 0, 0)).subtract(tube);
        RandomSource random = level.random;
        for (int i = 0; i < STREAM_GRAINS; i++) {
            double drop = (i + random.nextDouble()) / STREAM_GRAINS * STREAM_LENGTH;
            double jx = (random.nextDouble() - 0.5) * 0.018, jz = (random.nextDouble() - 0.5) * 0.008;
            level.addParticle(ModParticles.GUNPOWDER.get(), tube.x + jx, tube.y - drop, tube.z + jz,
                    belt.x, -0.05 - random.nextDouble() * 0.02, belt.z);
        }
        // half the smoke puffs down from the tube, half up out of the casing
        for (int i = 0; i < SMOKE_WISPS; i++) {
            boolean fromCasing = i % 2 == 1;
            double vx = belt.x + (random.nextDouble() - 0.5) * 0.03, vz = belt.z + (random.nextDouble() - 0.5) * 0.03;
            double vy = fromCasing ? 0.008 + random.nextDouble() * 0.01 : -0.02 - random.nextDouble() * 0.015;
            level.addParticle(ModParticles.POWDER_SMOKE.get(), tube.x, tube.y - (fromCasing ? STREAM_LENGTH : 0), tube.z,
                    vx, vy, vz);
        }
    }

    // ---- Interaction ----

    // Casings dropped onto the start of the belt go onto it
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

    // Right-clicking either front screen opens the GUI
    @Override
    public InteractionResult click(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player,
                                   InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        if (!SCREEN_BLOCKS.contains(posInMultiblock)) return InteractionResult.PASS;
        if (!isClient) {
            BlockPos clicked = ctx.getLevel().toAbsolute(posInMultiblock);
            player.openMenu(new SimpleMenuProvider(
                    (id, inventory, p) -> AmmoAssemblerMenu.makeServer(id, inventory, new MultiblockMenuContext<>(ctx, clicked)),
                    Component.translatable("gui.immersive_munitions.ammo_assembler")));
        }
        return InteractionResult.SUCCESS;
    }

    // ---- Hoppers, capabilities and drops ----

    // One-slot hoppers; the GUI's client side builds the same ones so it filters items the same way
    static ItemStackHandler gunpowderSlot(Runnable markDirty) {
        return slot(markDirty, stack -> stack.is(Tags.Items.GUNPOWDER));
    }

    static ItemStackHandler projectileSlot(Runnable markDirty) {
        return slot(markDirty, stack -> stack.is(ModTags.PROJECTILES));
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

    // The belt row is a low slab so dropped items can land on it
    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType) {
        return pos -> pos.getY() == 1 && pos.getZ() == 1
                ? Shapes.box(0, 0, 0, 1, 0.125, 1)
                : Shapes.block();
    }

    /** The machine's saved and synced state: the belt, the two hoppers, the energy buffer and what the client shows. */
    public static class State implements IMultiblockState, ProcessContextInWorld<AmmoAssemblyRecipe> {
        public final RSState rsState = RSState.enabledByDefault();
        public final MultiblockProcessor<AmmoAssemblyRecipe, ProcessContextInWorld<AmmoAssemblyRecipe>> processor;
        final ItemStackHandler gunpowder;
        final ItemStackHandler projectiles;
        final IItemHandler casingInput;
        private final AveragingEnergyStorage energy = new AveragingEnergyStorage(ENERGY_CAPACITY);
        private final DroppingMultiblockOutput output;

        // Synced to the client: working (runs its belt), powered (the front lamp), and the hoppers' contents (the
        // feeder's projectile and the front screens)
        private boolean active;
        private boolean powered;
        ItemStack shownGunpowder = ItemStack.EMPTY, shownProjectiles = ItemStack.EMPTY;
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
            gunpowder = gunpowderSlot(markDirty);
            projectiles = projectileSlot(markDirty);
            casingInput = new CasingInput(ctx.levelSupplier(), this);
            output = new DroppingMultiblockOutput(OUTPUT, ctx);

            casingCap = new StoredCapability<>(casingInput);
            gunpowderCap = new StoredCapability<>(gunpowder);
            projectileCap = new StoredCapability<>(projectiles);
        }

        // ---- Belt rules ----

        /** Takes a recipe's gunpowder or projectile from its hopper; false if there isn't enough. */
        boolean take(ItemStackHandler from, IngredientWithSize what, boolean simulate) {
            ItemStack stack = from.getStackInSlot(0);
            if (!what.test(stack) || stack.getCount() < what.getCount()) return false;
            if (!simulate) from.extractItem(0, what.getCount(), false);
            return true;
        }

        // A casing can't catch up to the one ahead while that one waits at a station
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

        // ---- GUI actions (server) ----

        /** What a jammed belt is waiting for: the first casing held up at a station, or EMPTY. */
        ItemStack waitingFor(Level level) {
            for (MultiblockProcess<AmmoAssemblyRecipe, ?> process : processor.getQueue())
                if (process instanceof AssemblyProcess casing) {
                    IngredientWithSize need = casing.waitingFor(this, level);
                    if (need == null) continue;
                    ItemStack[] stacks = need.getMatchingStacks();
                    return stacks.length == 0 ? ItemStack.EMPTY : stacks[0];
                }
            return ItemStack.EMPTY;
        }

        /**
         * Clear Belt: takes every casing that hasn't reached the press off the belt and gives its parts back to the
         * player (or drops them at their feet). Returns whether anything was taken.
         */
        boolean clearBelt(Level level, Player player) {
            Set<Long> cleared = new HashSet<>();
            for (MultiblockProcess<AmmoAssemblyRecipe, ?> process : processor.getQueue())
                if (process instanceof AssemblyProcess casing && casing.beforePress(level)) {
                    casing.parts(level).forEach(part -> ItemHandlerHelper.giveItemToPlayer(player, part));
                    cleared.add(casing.id());
                }
            if (cleared.isEmpty()) return false;
            // the queue is read-only, so rebuild it from its saved form without the cleared casings
            ListTag kept = new ListTag();
            if (processor.toNBT() instanceof ListTag all)
                for (Tag tag : all)
                    if (tag instanceof CompoundTag process && !cleared.contains(process.getLong("id"))) kept.add(process);
            processor.fromNBT(kept, AssemblyProcess::new);
            return true;
        }

        // ---- Client view ----

        public boolean isPowered() {
            return powered;
        }

        public boolean isActive() {
            return active;
        }

        public boolean hasGunpowder() {
            return !shownGunpowder.isEmpty();
        }

        public boolean hasProjectiles() {
            return !shownProjectiles.isEmpty();
        }

        /** The gunpowder hopper's contents as of the last sync. Don't modify. */
        public ItemStack shownGunpowder() {
            return shownGunpowder;
        }

        /** The projectile hopper's contents as of the last sync. Don't modify. */
        public ItemStack shownProjectiles() {
            return shownProjectiles;
        }

        public int clientFills() {
            return clientFills;
        }

        // ---- Saving and syncing ----

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
            nbt.putBoolean("powered", powered);
            if (!shownGunpowder.isEmpty()) nbt.put("gunpowder", shownGunpowder.save(new CompoundTag()));
            if (!shownProjectiles.isEmpty()) nbt.put("projectile", shownProjectiles.save(new CompoundTag()));
        }

        // Casings the client is already running keep their place unless they've drifted (see keepClientProgress)
        @Override
        public void readSyncNBT(CompoundTag nbt) {
            Map<Long, AssemblyProcess> running = new HashMap<>();
            for (MultiblockProcess<AmmoAssemblyRecipe, ?> process : processor.getQueue())
                if (process instanceof AssemblyProcess casing) running.put(casing.id(), casing);
            processor.fromNBT(nbt.get("processor"), AssemblyProcess::new);
            for (MultiblockProcess<AmmoAssemblyRecipe, ?> process : processor.getQueue())
                if (process instanceof AssemblyProcess casing && running.containsKey(casing.id()))
                    casing.keepClientProgress(running.get(casing.id()));
            active = nbt.getBoolean("active");
            powered = nbt.getBoolean("powered");
            shownGunpowder = nbt.contains("gunpowder") ? ItemStack.of(nbt.getCompound("gunpowder")) : ItemStack.EMPTY;
            shownProjectiles = nbt.contains("projectile") ? ItemStack.of(nbt.getCompound("projectile")) : ItemStack.EMPTY;
        }
    }

    /** The belt's input: each casing inserted (by hand, conveyor or hopper) starts its own process. */
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
