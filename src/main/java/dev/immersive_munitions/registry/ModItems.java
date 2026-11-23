package dev.immersive_munitions.registry;

import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Zinc and brass materials. Block items from ModBlocks are registered here too. */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ImmersiveMunitions.MODID);

    public static final RegistryObject<Item> RAW_ZINC     = register("raw_zinc");
    public static final RegistryObject<Item> ZINC_INGOT   = register("zinc_ingot");
    public static final RegistryObject<Item> ZINC_NUGGET  = register("zinc_nugget");
    public static final RegistryObject<Item> ZINC_DUST    = register("zinc_dust");
    public static final RegistryObject<Item> BRASS_INGOT  = register("brass_ingot");
    public static final RegistryObject<Item> BRASS_NUGGET = register("brass_nugget");
    public static final RegistryObject<Item> BRASS_DUST   = register("brass_dust");
    public static final RegistryObject<Item> BRASS_PLATE  = register("brass_plate");
    public static final RegistryObject<Item> BRASS_GEAR   = register("brass_gear");
    public static final RegistryObject<Item> BRASS_ROD    = register("brass_rod");
    public static final RegistryObject<Item> BRASS_WIRE   = register("brass_wire");

    private static RegistryObject<Item> register(String id) {
        return ITEMS.register(id, () -> new Item(new Item.Properties()));
    }

    private ModItems() {}
}
