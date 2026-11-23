package dev.immersive_munitions.registry;

import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Icon-only items for the creative tab and its section buttons (their textures have no caliber label). */
public final class CreativeIconItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ImmersiveMunitions.MODID);

    public static final RegistryObject<Item> EMPTY_CASING = register("tab_icon_empty_casing");
    public static final RegistryObject<Item> MOLD = register("tab_icon_mold");
    public static final RegistryObject<Item> PROJECTILE = register("tab_icon_projectile");
    public static final RegistryObject<Item> MATERIALS = register("tab_icon_materials");

    private static RegistryObject<Item> register(String id) {
        return ITEMS.register(id, () -> new Item(new Item.Properties()));
    }

    private CreativeIconItems() {}
}
