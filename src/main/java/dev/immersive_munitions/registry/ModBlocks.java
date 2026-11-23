package dev.immersive_munitions.registry;

import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ImmersiveMunitions.MODID);

    public static final RegistryObject<Block> ZINC_ORE = block("zinc_ore", () ->
            new DropExperienceBlock(Properties.copy(net.minecraft.world.level.block.Blocks.IRON_ORE), UniformInt.of(0, 2)));
    public static final RegistryObject<Block> DEEPSLATE_ZINC_ORE = block("deepslate_zinc_ore", () ->
            new DropExperienceBlock(Properties.copy(net.minecraft.world.level.block.Blocks.DEEPSLATE_IRON_ORE), UniformInt.of(0, 2)));
    public static final RegistryObject<Block> RAW_ZINC_BLOCK = block("raw_zinc_block", () ->
            new Block(Properties.copy(net.minecraft.world.level.block.Blocks.RAW_IRON_BLOCK)));
    public static final RegistryObject<Block> ZINC_BLOCK = block("zinc_block", () ->
            new Block(Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
    public static final RegistryObject<Block> BRASS_BLOCK = block("brass_block", () ->
            new Block(Properties.of().mapColor(MapColor.GOLD).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));

    private static RegistryObject<Block> block(String name, Supplier<Block> sup) {
        RegistryObject<Block> b = BLOCKS.register(name, sup);
        ModItems.ITEMS.register(name, () -> new BlockItem(b.get(), new Item.Properties()));
        return b;
    }
    private ModBlocks() {}
}
