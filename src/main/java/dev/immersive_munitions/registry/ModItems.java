package dev.immersive_munitions.registry;

import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ImmersiveMunitions.MODID);

    public static final RegistryObject<Item> RAW_ZINC     = simple("raw_zinc");
    public static final RegistryObject<Item> ZINC_INGOT   = simple("zinc_ingot");
    public static final RegistryObject<Item> ZINC_NUGGET  = simple("zinc_nugget");
    public static final RegistryObject<Item> ZINC_DUST    = simple("zinc_dust");
    public static final RegistryObject<Item> BRASS_INGOT  = simple("brass_ingot");
    public static final RegistryObject<Item> BRASS_NUGGET = simple("brass_nugget");
    public static final RegistryObject<Item> BRASS_DUST   = simple("brass_dust");
    public static final RegistryObject<Item> BRASS_PLATE  = simple("brass_plate");
    public static final RegistryObject<Item> BRASS_GEAR   = simple("brass_gear");
    public static final RegistryObject<Item> BRASS_ROD    = simple("brass_rod");
    public static final RegistryObject<Item> BRASS_WIRE   = simple("brass_wire");

    private static RegistryObject<Item> simple(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }
    private ModItems() {}
}
