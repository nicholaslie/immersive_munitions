package dev.immersive_munitions.registry;

import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Icon-only items for the creative tab and its section buttons (their textures have no caliber label). */
public final class CreativeIconItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ImmersiveMunitions.MODID);

    public static final DeferredHolder<Item, Item> EMPTY_CASING = register("tab_icon_empty_casing");
    public static final DeferredHolder<Item, Item> MOLD = register("tab_icon_mold");
    public static final DeferredHolder<Item, Item> PROJECTILE = register("tab_icon_projectile");
    public static final DeferredHolder<Item, Item> MATERIALS = register("tab_icon_materials");

    private static DeferredHolder<Item, Item> register(String id) {
        return ITEMS.register(id, () -> new Item(new Item.Properties()));
    }

    private CreativeIconItems() {}
}
