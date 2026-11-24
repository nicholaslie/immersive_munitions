package dev.immersive_munitions.multiblock;

import blusunrize.immersiveengineering.api.energy.IMutableEnergyStorage;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.common.gui.IEContainerMenu;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import dev.immersive_munitions.multiblock.AmmoAssemblerLogic.State;
import dev.immersive_munitions.registry.ModMenus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The Ammunition Assembler's GUI, opened from either front screen: its stored energy, the gunpowder and projectile
 * hoppers as two slots the player can fill and empty, what a jammed belt is waiting for, and Clear Belt. Built on IE's
 * container menu, so shift-clicking, the data sync and screen messages work like IE's machines.
 */
public class AmmoAssemblerMenu extends IEContainerMenu {
    // Slot positions; the background (art/gui/AssemblerGui.java) draws their frames at these
    public static final int GUNPOWDER_X = 56, PROJECTILE_X = 92, SLOT_Y = 34;
    public static final int GUNPOWDER_SLOT = 0, PROJECTILE_SLOT = 1;
    // Screen -> server: the Clear Belt button
    public static final String CLEAR_BELT = "clear_belt";

    public final IMutableEnergyStorage energy;
    private final Player player;
    // Server only: takes the unfinished casings off the belt for a player
    @Nullable
    private final Consumer<Player> clearBelt;
    // Synced: the item a jammed belt is waiting for, as its registry id (-1 when it isn't jammed)
    private int waitingFor = -1;

    public static AmmoAssemblerMenu makeServer(int id, Inventory player, MultiblockMenuContext<State> ctx) {
        IMultiblockContext<State> mb = ctx.mbContext();
        State state = mb.getState();
        Level level = mb.getLevel().getRawLevel();
        Supplier<Integer> waiting = () -> {
            ItemStack need = state.waitingFor(level);
            return need.isEmpty() ? -1 : BuiltInRegistries.ITEM.getId(need.getItem());
        };
        Consumer<Player> clear = p -> {
            if (!state.clearBelt(level, p)) return;
            mb.markMasterDirty();
            mb.requestMasterBESync();
        };
        return new AmmoAssemblerMenu(multiblockCtx(ModMenus.AMMO_ASSEMBLER.get(), id, ctx), player,
                state.gunpowder, state.projectiles, state.getEnergy(), waiting, clear);
    }

    public static AmmoAssemblerMenu makeClient(int id, Inventory player) {
        Runnable none = () -> {};
        return new AmmoAssemblerMenu(clientCtx(ModMenus.AMMO_ASSEMBLER.get(), id), player,
                AmmoAssemblerLogic.gunpowderSlot(none), AmmoAssemblerLogic.projectileSlot(none),
                new MutableEnergyStorage(AmmoAssemblerLogic.ENERGY_CAPACITY), null, null);
    }

    private AmmoAssemblerMenu(MenuContext ctx, Inventory player, IItemHandler gunpowder, IItemHandler projectiles,
                              IMutableEnergyStorage energy, @Nullable Supplier<Integer> waiting,
                              @Nullable Consumer<Player> clearBelt) {
        super(ctx);
        this.energy = energy;
        this.player = player.player;
        this.clearBelt = clearBelt;
        addSlot(new SlotItemHandler(gunpowder, 0, GUNPOWDER_X, SLOT_Y));
        addSlot(new SlotItemHandler(projectiles, 0, PROJECTILE_X, SLOT_Y));
        ownSlotCount = 2;

        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(player, col + row * 9 + 9, 8 + col * 18, 85 + row * 18));
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(player, col, 8 + col * 18, 143));

        addGenericData(GenericContainerData.energy(energy));
        addGenericData(GenericContainerData.int32(waiting != null ? waiting : () -> waitingFor, v -> waitingFor = v));
    }

    /** Client: the item a jammed belt is waiting for, or EMPTY if it isn't jammed. */
    public ItemStack waitingFor() {
        return waitingFor < 0 ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.byId(waitingFor));
    }

    @Override
    public void receiveMessageFromScreen(CompoundTag message) {
        if (message.getBoolean(CLEAR_BELT) && clearBelt != null) clearBelt.accept(player);
    }
}
