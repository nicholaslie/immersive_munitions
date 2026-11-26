package dev.immersive_munitions.registry;

import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

/** Zinc and brass blocks: ores, raw zinc block and storage blocks. */
public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, ImmersiveMunitions.MODID);

    public static final DeferredHolder<Block, Block> ZINC_ORE = register("zinc_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 2), Properties.ofFullCopy(Blocks.IRON_ORE)));
    public static final DeferredHolder<Block, Block> DEEPSLATE_ZINC_ORE = register("deepslate_zinc_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 2), Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)));
    public static final DeferredHolder<Block, Block> RAW_ZINC_BLOCK = register("raw_zinc_block",
            () -> new Block(Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)));
    public static final DeferredHolder<Block, Block> ZINC_BLOCK = register("zinc_block",
            () -> new Block(metalBlock(MapColor.METAL)));
    public static final DeferredHolder<Block, Block> BRASS_BLOCK = register("brass_block",
            () -> new Block(metalBlock(MapColor.GOLD)));

    private static Properties metalBlock(MapColor color) {
        return Properties.of()
                .mapColor(color)
                .strength(5.0F, 6.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL);
    }

    /** Registers the block and its block item (the item goes in ModItems.ITEMS). */
    private static DeferredHolder<Block, Block> register(String name, Supplier<Block> factory) {
        DeferredHolder<Block, Block> block = BLOCKS.register(name, factory);
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    private ModBlocks() {}
}
