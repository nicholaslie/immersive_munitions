package dev.immersive_munitions.registry;

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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModMultiblocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ImmersiveMunitions.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ImmersiveMunitions.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ImmersiveMunitions.MODID);

    public static final MultiblockRegistration<AmmoAssemblerLogic.State> AMMO_ASSEMBLER =
            new Builder<>(new AmmoAssemblerLogic(), "ammo_assembler")
                    .structure(() -> AmmoAssemblerMultiblock.INSTANCE)
                    .notMirrored()
                    .redstone(state -> state.rsState, AmmoAssemblerLogic.REDSTONE_POS)
                    .defaultBEs(BLOCK_ENTITIES)
                    .defaultBlock(BLOCKS, ITEMS, metalMachine())
                    .build();

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
