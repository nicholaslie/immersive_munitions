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
    // Roof intakes over the belt row, right above their screens (gunpowder over the first bay, projectiles over the middle bay),
    // and IE's power port on the roof over the press bay, back row, facing up for a wire connector (as IE does it)
    private static final CapabilityPosition GUNPOWDER_INPUT = new CapabilityPosition(0, 2, 1, RelativeBlockFace.UP);
    private static final CapabilityPosition PROJECTILE_INPUT = new CapabilityPosition(1, 2, 1, RelativeBlockFace.UP);
    private static final CapabilityPosition ENERGY_INPUT = new CapabilityPosition(2, 2, 0, RelativeBlockFace.UP);

    // How far along the belt (0..1) each station sits: one per block, centred on it
    public static final float FILL_POINT = 1 / 6f;
    public static final float PLACE_POINT = 0.5f;
    public static final float PRESS_POINT = 5 / 6f;
    // Casings keep at least this much of a cycle between each other. With a 60-tick recipe that is one casing
    // every 7.5 ticks, slightly faster than a hopper (8 ticks per item), so a hopper feed never backs up.
    private static final float MIN_SPACING = 1 / 8f;
    // Enough room on the belt for a full cycle's worth of casings at that spacing
    private static final int MAX_CASINGS = 8;
    private static final int RESYNC_TICKS = 20, COUNT_SYNC_TICKS = 5;
    public static final int ENERGY_CAPACITY = 16000;
    // The two front screens (in the top row over the belt, under the intakes): right-clicking either opens the GUI
    private static final Set<BlockPos> SCREEN_BLOCKS = Set.of(new BlockPos(0, 2, 1), new BlockPos(1, 2, 1));
    // Bottom of the powder head's drop tube at the bottom of its stroke. Its stream of powder: how many grains, how
    // far below the tube they reach (about to the casing's mouth); the belt carries a casing 3 blocks per cycle
    private static final Vec3 DROP_TUBE_TIP = new Vec3(0.5, 1.37, 1.5);
    private static final int STREAM_GRAINS = 30, SMOKE_WISPS = 6;
    private static final double STREAM_LENGTH = 0.12, BELT_BLOCKS = 3;

    @Override
    public State createInitialState(IInitialMultiblockContext<State> ctx) {
        return new State(ctx);
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        boolean active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        boolean powered = state.energy.getEnergyStored() > 0;
        ItemStack powder = state.gunpowder.getStackInSlot(0), projectile = state.projectiles.getStackInSlot(0);
        state.ticksSinceSync++;
        // The client runs the belt itself (tickClient); resync when what it relies on changes, and now and then
        // while running so its casings don't drift. The screens' counts only need to keep roughly up, so a change
        // in count alone waits a few ticks (a hopper and the belt each change them every 8 ticks or so)
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

    // A quick little stream of powder from the drop tube into the casing it fills: grains strung out between the
    // tube and the casing's mouth, falling fast, and carried along with the casing at the belt's speed
    private static void pourPowder(IMultiblockLevel mbLevel, Level level, int maxTicks) {
        if (maxTicks <= 0) return;
        Vec3 tube = mbLevel.toAbsolute(DROP_TUBE_TIP);
        Vec3 belt = mbLevel.toAbsolute(DROP_TUBE_TIP.add(BELT_BLOCKS / maxTicks, 0, 0)).subtract(tube);
        RandomSource random = level.random;
        for (int i = 0; i < STREAM_GRAINS; i++) {
            // evenly spaced down the stream (a little jitter), so the grains overlap into one unbroken line
            double drop = (i + random.nextDouble()) / STREAM_GRAINS * STREAM_LENGTH;
            double jx = (random.nextDouble() - 0.5) * 0.018, jz = (random.nextDouble() - 0.5) * 0.008;
            level.addParticle(ModParticles.GUNPOWDER.get(), tube.x + jx, tube.y - drop, tube.z + jz,
                    belt.x, -0.05 - random.nextDouble() * 0.02, belt.z);
        }
        // and a small poof of smoke: half out of the tube's mouth, puffing down, half kicked up out of the casing's
        // mouth as the powder lands, both spreading sideways and riding along with the casing
        for (int i = 0; i < SMOKE_WISPS; i++) {
            boolean fromCasing = i % 2 == 1;
            double vx = belt.x + (random.nextDouble() - 0.5) * 0.03, vz = belt.z + (random.nextDouble() - 0.5) * 0.03;
            double vy = fromCasing ? 0.008 + random.nextDouble() * 0.01 : -0.02 - random.nextDouble() * 0.015;
            level.addParticle(ModParticles.POWDER_SMOKE.get(), tube.x, tube.y - (fromCasing ? STREAM_LENGTH : 0), tube.z,
                    vx, vy, vz);
        }
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

    /** The gunpowder hopper: one slot, gunpowder only. Shared with the GUI's client side so it filters the same. */
    static ItemStackHandler gunpowderSlot(Runnable markDirty) {
        return slot(markDirty, stack -> stack.is(Tags.Items.GUNPOWDER));
    }

    /** The projectile hopper: one slot, projectiles only. */
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
        private final AveragingEnergyStorage energy = new AveragingEnergyStorage(ENERGY_CAPACITY);
        private final DroppingMultiblockOutput output;
        // Synced: whether the machine is working (runs the client's belt)
        private boolean active;
        // Synced: whether it has any energy stored, which lights the power lamp on the front (it keeps working off
        // its buffer, so the lamp stays lit until that runs dry)
        private boolean powered;
        // Synced copies of what's in the two hoppers, as of the last sync: the client runs the belt by them, the
        // feeder's jaws hold one of the projectiles, and the front screens show the counts
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

        /** Server: what the belt is held up waiting for (the first casing stopped at a station), or EMPTY. */
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
         * Server: takes every casing that hasn't reached the press off the belt and gives the player what it's made of
         * so far (casing, gunpowder, projectile), into their inventory or at their feet if it's full. Rounds at the
         * press finish as normal. Returns whether anything was taken.
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

        /** Client: the gunpowder hopper's contents, as of the last sync. Don't modify. */
        public ItemStack shownGunpowder() {
            return shownGunpowder;
        }

        /** Client: the projectile hopper's contents, as of the last sync. Don't modify. */
        public ItemStack shownProjectiles() {
            return shownProjectiles;
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
            nbt.putBoolean("powered", powered);
            if (!shownGunpowder.isEmpty()) nbt.put("gunpowder", shownGunpowder.save(new CompoundTag()));
            if (!shownProjectiles.isEmpty()) nbt.put("projectile", shownProjectiles.save(new CompoundTag()));
        }

        @Override
        public void readSyncNBT(CompoundTag nbt) {
            // Casings the client is already running keep their place unless they have drifted (see keepClientProgress)
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
