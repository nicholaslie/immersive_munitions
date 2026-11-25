package dev.immersive_munitions.registry;

import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Casings, projectiles and molds for each TaCZ caliber. The maps are keyed by TaCZ ammo id, e.g. "9mm". */
public final class AmmoComponentItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ImmersiveMunitions.MODID);

    public static final List<String> CALIBERS = List.of(
            "12g", "22wmr", "300blk", "308", "30_06", "338", "357mag", "40mm", "45_70", "45acp",
            "46x30", "500mag", "50ae", "50bmg", "545x39", "556x45", "57x28", "58x42", "68x51fury",
            "762x25", "762x39", "762x54", "792x57", "9mm");

    public static final Map<String, RegistryObject<Item>> EMPTY_CASINGS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> PROJECTILES = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> CASING_MOLDS = new LinkedHashMap<>();

    static {
        for (String caliber : CALIBERS) {
            EMPTY_CASINGS.put(caliber, register("empty_casing_" + caliber));
            PROJECTILES.put(caliber, register("projectile_" + caliber));
            CASING_MOLDS.put(caliber, register("mold_casing_" + caliber));
        }
    }

    private static RegistryObject<Item> register(String id) {
        return ITEMS.register(id, () -> new Item(new Item.Properties()));
    }

    private AmmoComponentItems() {}
}
