package dev.immersive_munitions.registry;

import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Casings, projectiles and molds for each caliber. The maps are keyed by caliber, the ammo id's path ("9mm" for
 * tacz:9mm, "454_casul" for immersive_armorer:454_casul); {@link #ammoId} gives the full id.
 */
public final class AmmoComponentItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ImmersiveMunitions.MODID);

    /** TaCZ's default gun pack. */
    public static final List<String> TACZ_CALIBERS = List.of(
            "12g", "22wmr", "300blk", "308", "30_06", "338", "357mag", "40mm", "45_70", "45acp",
            "46x30", "500mag", "50ae", "50bmg", "545x39", "556x45", "57x28", "58x42", "68x51fury",
            "762x25", "762x39", "762x54", "792x57", "9mm");
    /** The Immersive Armorer gun pack's cartridges (its other ammo is made in IE's machines, see ArmorerCompat). */
    public static final List<String> ARMORER_CALIBERS = List.of("454_casul", "ap_ammo", "20mm");
    public static final List<String> CALIBERS = Stream.concat(TACZ_CALIBERS.stream(), ARMORER_CALIBERS.stream()).toList();

    /** The full TaCZ ammo id for a caliber. */
    public static ResourceLocation ammoId(String caliber) {
        return ResourceLocation.fromNamespaceAndPath(
                ARMORER_CALIBERS.contains(caliber) ? "immersive_armorer" : "tacz", caliber);
    }

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
