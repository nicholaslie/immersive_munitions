package dev.immersive_munitions.compat;

import blusunrize.immersiveengineering.api.IEApi;
import dev.immersive_munitions.Config;
import dev.immersive_munitions.registry.ModBlocks;
import dev.immersive_munitions.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Steps aside for mods that already have our materials. With Create installed, our zinc and the brass items Create
 * also makes (ingot, nugget, block, plate) are hidden; with Immersive Geology, all our zinc is. Hidden items stay
 * registered, so old worlds keep them, but they leave the creative tab and JEI, their own recipes switch off (load
 * conditions in their recipe files), and IE's machines prefer the other mod's item where both are in a tag.
 */
public final class MaterialCompat {
    public static final String CREATE = "create", GEOLOGY = "immersivegeology";

    private static Set<Item> hidden;

    public static boolean loaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    /** Whether our zinc ore should step aside for this mod's own ores (each has a config switch). */
    public static boolean yieldsWorldgenTo(String modId) {
        if (!loaded(modId)) return false;
        return switch (modId) {
            case GEOLOGY -> Config.YIELD_TO_GEOLOGY.get();
            case CREATE -> Config.YIELD_TO_CREATE.get();
            default -> true;
        };
    }

    public static boolean isHidden(Item item) {
        return hiddenItems().contains(item);
    }

    public static Set<Item> hiddenItems() {
        if (hidden == null) {
            Set<Item> set = new HashSet<>();
            boolean create = loaded(CREATE), geology = loaded(GEOLOGY);
            if (create || geology) {
                items(set, ModItems.RAW_ZINC, ModItems.ZINC_INGOT, ModItems.ZINC_NUGGET);
                blocks(set, ModBlocks.ZINC_ORE, ModBlocks.DEEPSLATE_ZINC_ORE, ModBlocks.RAW_ZINC_BLOCK, ModBlocks.ZINC_BLOCK);
            }
            if (geology) items(set, ModItems.ZINC_DUST);   // Create has no zinc dust, so it stays with Create alone
            if (create) {
                items(set, ModItems.BRASS_INGOT, ModItems.BRASS_NUGGET, ModItems.BRASS_PLATE);
                blocks(set, ModBlocks.BRASS_BLOCK);
            }
            hidden = set;
        }
        return hidden;
    }

    @SafeVarargs
    private static void items(Set<Item> set, DeferredHolder<Item, Item>... items) {
        for (DeferredHolder<Item, Item> item : items) set.add(item.get());
    }

    @SafeVarargs
    private static void blocks(Set<Item> set, DeferredHolder<Block, ? extends Block>... blocks) {
        for (DeferredHolder<Block, ? extends Block> block : blocks) set.add(block.get().asItem());
    }

    /**
     * IE picks a tag's output by its mod preference list, then alphabetically, and "immersive_munitions" sorts before
     * "immersivegeology". Adding the other mods to the end of IE's list makes its machines give their items instead of
     * ours. Call after IE's config has loaded (common setup).
     */
    public static void preferOtherMods() {
        List<String> preference = new ArrayList<>(IEApi.modPreference);
        for (String modId : List.of(GEOLOGY, CREATE))
            if (loaded(modId) && !preference.contains(modId)) preference.add(modId);
        IEApi.modPreference = preference;
    }

    private MaterialCompat() {}
}
