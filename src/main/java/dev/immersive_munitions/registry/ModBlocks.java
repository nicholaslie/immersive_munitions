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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/** Zinc and brass blocks: ores, raw zinc block and storage blocks. */
public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ImmersiveMunitions.MODID);

    public static final RegistryObject<Block> ZINC_ORE = register("zinc_ore",
            () -> new DropExperienceBlock(Properties.copy(Blocks.IRON_ORE), UniformInt.of(0, 2)));
    public static final RegistryObject<Block> DEEPSLATE_ZINC_ORE = register("deepslate_zinc_ore",
            () -> new DropExperienceBlock(Properties.copy(Blocks.DEEPSLATE_IRON_ORE), UniformInt.of(0, 2)));
    public static final RegistryObject<Block> RAW_ZINC_BLOCK = register("raw_zinc_block",
            () -> new Block(Properties.copy(Blocks.RAW_IRON_BLOCK)));
    public static final RegistryObject<Block> ZINC_BLOCK = register("zinc_block",
            () -> new Block(metalBlock(MapColor.METAL)));
    public static final RegistryObject<Block> BRASS_BLOCK = register("brass_block",
            () -> new Block(metalBlock(MapColor.GOLD)));

    private static Properties metalBlock(MapColor color) {
        return Properties.of()
                .mapColor(color)
                .strength(5.0F, 6.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL);
    }

    /** Registers the block and its block item (the item goes in ModItems.ITEMS). */
    private static RegistryObject<Block> register(String name, Supplier<Block> factory) {
        RegistryObject<Block> block = BLOCKS.register(name, factory);
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    private ModBlocks() {}
}
