package dev.immersive_munitions.registry;

import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Hidden-purpose item stacks whose dedicated art omits the caliber corner label. */
public final class CreativeIconItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ImmersiveMunitions.MODID);
    public static final RegistryObject<Item> EMPTY_CASING = icon("tab_icon_empty_casing");
    public static final RegistryObject<Item> MOLD = icon("tab_icon_mold");
    public static final RegistryObject<Item> PROJECTILE = icon("tab_icon_projectile");
    public static final RegistryObject<Item> MATERIALS = icon("tab_icon_materials");
    private static RegistryObject<Item> icon(String name) { return ITEMS.register(name, () -> new Item(new Item.Properties())); }
    private CreativeIconItems() {}
}
