package dev.immersive_munitions.registry;

import net.neoforged.bus.api.IEventBus;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistrationBuilder;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent.StateWrapper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import dev.immersive_munitions.ImmersiveMunitions;
import dev.immersive_munitions.multiblock.AmmoAssemblerLogic;
import dev.immersive_munitions.multiblock.AmmoAssemblerMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** The Ammunition Assembler multiblock, registered through IE's multiblock builder. */
public final class ModMultiblocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, ImmersiveMunitions.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ImmersiveMunitions.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ImmersiveMunitions.MODID);

    // IE's builder hooks the multiblock's capabilities onto the mod bus; ImmersiveMunitions passes the bus in
    private static final List<Consumer<IEventBus>> MOD_BUS_LISTENERS = new ArrayList<>();

    public static final MultiblockRegistration<AmmoAssemblerLogic.State> AMMO_ASSEMBLER =
            new Builder<>(new AmmoAssemblerLogic(), "ammo_assembler")
                    .structure(() -> AmmoAssemblerMultiblock.INSTANCE)
                    .notMirrored()
                    .redstone(state -> state.rsState, AmmoAssemblerLogic.REDSTONE_POS)
                    .defaultBEs(BLOCK_ENTITIES)
                    .defaultBlock(BLOCKS, ITEMS, metalMachine())
                    .build(MOD_BUS_LISTENERS::add);

    /** Adds the multiblocks' mod bus listeners (capabilities). Call from the mod constructor. */
    public static void registerModBusListeners(IEventBus bus) {
        MOD_BUS_LISTENERS.forEach(listener -> listener.accept(bus));
    }

    /** Hooks the structures into IE's hammer. Call during common setup. */
    public static void registerStructures() {
        MultiblockHandler.registerMultiblock(AmmoAssemblerMultiblock.INSTANCE);
    }

    // Same feel as IE's metal multiblocks
    private static Properties metalMachine() {
        return Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(3, 15)
                .requiresCorrectToolForDrops()
                .isViewBlocking((state, level, pos) -> false)
                .noOcclusion();
    }

    private static class Builder<S extends IMultiblockState> extends MultiblockRegistrationBuilder<S, Builder<S>> {
        Builder(IMultiblockLogic<S> logic, String name) {
            super(logic, ResourceLocation.fromNamespaceAndPath(ImmersiveMunitions.MODID, name));
        }

        Builder<S> redstone(StateWrapper<S, RSState> getState, BlockPos... positions) {
            redstoneAware();
            return selfWrappingComponent(new RedstoneControl<>(getState, positions));
        }

        @Override
        protected Builder<S> self() {
            return this;
        }
    }

    private ModMultiblocks() {}
}
