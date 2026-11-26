package dev.immersive_munitions.registry;

import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Zinc and brass materials. Block items from ModBlocks are registered here too. */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ImmersiveMunitions.MODID);

    public static final DeferredHolder<Item, Item> RAW_ZINC     = register("raw_zinc");
    public static final DeferredHolder<Item, Item> ZINC_INGOT   = register("zinc_ingot");
    public static final DeferredHolder<Item, Item> ZINC_NUGGET  = register("zinc_nugget");
    public static final DeferredHolder<Item, Item> ZINC_DUST    = register("zinc_dust");
    public static final DeferredHolder<Item, Item> BRASS_INGOT  = register("brass_ingot");
    public static final DeferredHolder<Item, Item> BRASS_NUGGET = register("brass_nugget");
    public static final DeferredHolder<Item, Item> BRASS_PLATE  = register("brass_plate");

    private static DeferredHolder<Item, Item> register(String id) {
        return ITEMS.register(id, () -> new Item(new Item.Properties()));
    }

    private ModItems() {}
}
