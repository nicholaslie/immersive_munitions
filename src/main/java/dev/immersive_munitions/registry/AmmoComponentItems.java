package dev.immersive_munitions.registry;

import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

/** Items used as the physical components in ammunition assembly. */
public final class AmmoComponentItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ImmersiveMunitions.MODID);
    public static final Map<String, RegistryObject<Item>> EMPTY_CASINGS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> PROJECTILES = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> CASING_MOLDS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> CATEGORY_MOLDS = new LinkedHashMap<>();

    public static final RegistryObject<Item> EMPTY_CASING_12G = register(EMPTY_CASINGS, "empty_casing_12g");
    public static final RegistryObject<Item> PROJECTILE_12G = register(PROJECTILES, "projectile_12g");
    public static final RegistryObject<Item> CASING_MOLD_12G = register(CASING_MOLDS, "mold_casing_12g");
    public static final RegistryObject<Item> EMPTY_CASING_22WMR = register(EMPTY_CASINGS, "empty_casing_22wmr");
    public static final RegistryObject<Item> PROJECTILE_22WMR = register(PROJECTILES, "projectile_22wmr");
    public static final RegistryObject<Item> CASING_MOLD_22WMR = register(CASING_MOLDS, "mold_casing_22wmr");
    public static final RegistryObject<Item> EMPTY_CASING_300BLK = register(EMPTY_CASINGS, "empty_casing_300blk");
    public static final RegistryObject<Item> PROJECTILE_300BLK = register(PROJECTILES, "projectile_300blk");
    public static final RegistryObject<Item> CASING_MOLD_300BLK = register(CASING_MOLDS, "mold_casing_300blk");
    public static final RegistryObject<Item> EMPTY_CASING_308 = register(EMPTY_CASINGS, "empty_casing_308");
    public static final RegistryObject<Item> PROJECTILE_308 = register(PROJECTILES, "projectile_308");
    public static final RegistryObject<Item> CASING_MOLD_308 = register(CASING_MOLDS, "mold_casing_308");
    public static final RegistryObject<Item> EMPTY_CASING_30_06 = register(EMPTY_CASINGS, "empty_casing_30_06");
    public static final RegistryObject<Item> PROJECTILE_30_06 = register(PROJECTILES, "projectile_30_06");
    public static final RegistryObject<Item> CASING_MOLD_30_06 = register(CASING_MOLDS, "mold_casing_30_06");
    public static final RegistryObject<Item> EMPTY_CASING_338 = register(EMPTY_CASINGS, "empty_casing_338");
    public static final RegistryObject<Item> PROJECTILE_338 = register(PROJECTILES, "projectile_338");
    public static final RegistryObject<Item> CASING_MOLD_338 = register(CASING_MOLDS, "mold_casing_338");
    public static final RegistryObject<Item> EMPTY_CASING_357MAG = register(EMPTY_CASINGS, "empty_casing_357mag");
    public static final RegistryObject<Item> PROJECTILE_357MAG = register(PROJECTILES, "projectile_357mag");
    public static final RegistryObject<Item> CASING_MOLD_357MAG = register(CASING_MOLDS, "mold_casing_357mag");
    public static final RegistryObject<Item> EMPTY_CASING_40MM = register(EMPTY_CASINGS, "empty_casing_40mm");
    public static final RegistryObject<Item> PROJECTILE_40MM = register(PROJECTILES, "projectile_40mm");
    public static final RegistryObject<Item> CASING_MOLD_40MM = register(CASING_MOLDS, "mold_casing_40mm");
    public static final RegistryObject<Item> EMPTY_CASING_45_70 = register(EMPTY_CASINGS, "empty_casing_45_70");
    public static final RegistryObject<Item> PROJECTILE_45_70 = register(PROJECTILES, "projectile_45_70");
    public static final RegistryObject<Item> CASING_MOLD_45_70 = register(CASING_MOLDS, "mold_casing_45_70");
    public static final RegistryObject<Item> EMPTY_CASING_45ACP = register(EMPTY_CASINGS, "empty_casing_45acp");
    public static final RegistryObject<Item> PROJECTILE_45ACP = register(PROJECTILES, "projectile_45acp");
    public static final RegistryObject<Item> CASING_MOLD_45ACP = register(CASING_MOLDS, "mold_casing_45acp");
    public static final RegistryObject<Item> EMPTY_CASING_46X30 = register(EMPTY_CASINGS, "empty_casing_46x30");
    public static final RegistryObject<Item> PROJECTILE_46X30 = register(PROJECTILES, "projectile_46x30");
    public static final RegistryObject<Item> CASING_MOLD_46X30 = register(CASING_MOLDS, "mold_casing_46x30");
    public static final RegistryObject<Item> EMPTY_CASING_500MAG = register(EMPTY_CASINGS, "empty_casing_500mag");
    public static final RegistryObject<Item> PROJECTILE_500MAG = register(PROJECTILES, "projectile_500mag");
    public static final RegistryObject<Item> CASING_MOLD_500MAG = register(CASING_MOLDS, "mold_casing_500mag");
    public static final RegistryObject<Item> EMPTY_CASING_50AE = register(EMPTY_CASINGS, "empty_casing_50ae");
    public static final RegistryObject<Item> PROJECTILE_50AE = register(PROJECTILES, "projectile_50ae");
    public static final RegistryObject<Item> CASING_MOLD_50AE = register(CASING_MOLDS, "mold_casing_50ae");
    public static final RegistryObject<Item> EMPTY_CASING_50BMG = register(EMPTY_CASINGS, "empty_casing_50bmg");
    public static final RegistryObject<Item> PROJECTILE_50BMG = register(PROJECTILES, "projectile_50bmg");
    public static final RegistryObject<Item> CASING_MOLD_50BMG = register(CASING_MOLDS, "mold_casing_50bmg");
    public static final RegistryObject<Item> EMPTY_CASING_545X39 = register(EMPTY_CASINGS, "empty_casing_545x39");
    public static final RegistryObject<Item> PROJECTILE_545X39 = register(PROJECTILES, "projectile_545x39");
    public static final RegistryObject<Item> CASING_MOLD_545X39 = register(CASING_MOLDS, "mold_casing_545x39");
    public static final RegistryObject<Item> EMPTY_CASING_556X45 = register(EMPTY_CASINGS, "empty_casing_556x45");
    public static final RegistryObject<Item> PROJECTILE_556X45 = register(PROJECTILES, "projectile_556x45");
    public static final RegistryObject<Item> CASING_MOLD_556X45 = register(CASING_MOLDS, "mold_casing_556x45");
    public static final RegistryObject<Item> EMPTY_CASING_57X28 = register(EMPTY_CASINGS, "empty_casing_57x28");
    public static final RegistryObject<Item> PROJECTILE_57X28 = register(PROJECTILES, "projectile_57x28");
    public static final RegistryObject<Item> CASING_MOLD_57X28 = register(CASING_MOLDS, "mold_casing_57x28");
    public static final RegistryObject<Item> EMPTY_CASING_58X42 = register(EMPTY_CASINGS, "empty_casing_58x42");
    public static final RegistryObject<Item> PROJECTILE_58X42 = register(PROJECTILES, "projectile_58x42");
    public static final RegistryObject<Item> CASING_MOLD_58X42 = register(CASING_MOLDS, "mold_casing_58x42");
    public static final RegistryObject<Item> EMPTY_CASING_68X51FURY = register(EMPTY_CASINGS, "empty_casing_68x51fury");
    public static final RegistryObject<Item> PROJECTILE_68X51FURY = register(PROJECTILES, "projectile_68x51fury");
    public static final RegistryObject<Item> CASING_MOLD_68X51FURY = register(CASING_MOLDS, "mold_casing_68x51fury");
    public static final RegistryObject<Item> EMPTY_CASING_762X25 = register(EMPTY_CASINGS, "empty_casing_762x25");
    public static final RegistryObject<Item> PROJECTILE_762X25 = register(PROJECTILES, "projectile_762x25");
    public static final RegistryObject<Item> CASING_MOLD_762X25 = register(CASING_MOLDS, "mold_casing_762x25");
    public static final RegistryObject<Item> EMPTY_CASING_762X39 = register(EMPTY_CASINGS, "empty_casing_762x39");
    public static final RegistryObject<Item> PROJECTILE_762X39 = register(PROJECTILES, "projectile_762x39");
    public static final RegistryObject<Item> CASING_MOLD_762X39 = register(CASING_MOLDS, "mold_casing_762x39");
    public static final RegistryObject<Item> EMPTY_CASING_762X54 = register(EMPTY_CASINGS, "empty_casing_762x54");
    public static final RegistryObject<Item> PROJECTILE_762X54 = register(PROJECTILES, "projectile_762x54");
    public static final RegistryObject<Item> CASING_MOLD_762X54 = register(CASING_MOLDS, "mold_casing_762x54");
    public static final RegistryObject<Item> EMPTY_CASING_792X57 = register(EMPTY_CASINGS, "empty_casing_792x57");
    public static final RegistryObject<Item> PROJECTILE_792X57 = register(PROJECTILES, "projectile_792x57");
    public static final RegistryObject<Item> CASING_MOLD_792X57 = register(CASING_MOLDS, "mold_casing_792x57");
    public static final RegistryObject<Item> EMPTY_CASING_9MM = register(EMPTY_CASINGS, "empty_casing_9mm");
    public static final RegistryObject<Item> PROJECTILE_9MM = register(PROJECTILES, "projectile_9mm");
    public static final RegistryObject<Item> CASING_MOLD_9MM = register(CASING_MOLDS, "mold_casing_9mm");

    public static final RegistryObject<Item> MOLD_DMR = register(CATEGORY_MOLDS, "mold_dmr");
    public static final RegistryObject<Item> MOLD_MG = register(CATEGORY_MOLDS, "mold_mg");
    public static final RegistryObject<Item> MOLD_PISTOL = register(CATEGORY_MOLDS, "mold_pistol");
    public static final RegistryObject<Item> MOLD_RIFLE = register(CATEGORY_MOLDS, "mold_rifle");
    public static final RegistryObject<Item> MOLD_RPG = register(CATEGORY_MOLDS, "mold_rpg");
    public static final RegistryObject<Item> MOLD_SHOTGUN = register(CATEGORY_MOLDS, "mold_shotgun");
    public static final RegistryObject<Item> MOLD_SMG = register(CATEGORY_MOLDS, "mold_smg");
    public static final RegistryObject<Item> MOLD_SNIPER = register(CATEGORY_MOLDS, "mold_sniper");

    private static RegistryObject<Item> register(Map<String, RegistryObject<Item>> map, String id) {
        RegistryObject<Item> item = ITEMS.register(id, () -> new Item(new Item.Properties()));
        map.put(id, item);
        return item;
    }
    private AmmoComponentItems() {}
}
