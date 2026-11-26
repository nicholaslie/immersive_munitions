package dev.immersive_munitions.compat;

import blusunrize.immersiveengineering.common.register.IEItems;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import dev.immersive_munitions.registry.AmmoComponentItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Support for the Immersive Armorer gun pack (Koei, CC BY-NC-ND 4.0; nothing of it is included). Its three
 * cartridges get casings, projectiles and molds like TaCZ's (on blueprints of their own), made into rounds by the Ammunition Assembler; its other
 * ammo comes from IE's machines: cut steel rods from the Metal Press, the three capacitor cells from the Engineer's
 * Workbench (on the {@value #CELLS_BLUEPRINT} blueprint), and the flammable chemical from the Bottling Machine.
 *
 * A gun pack isn't a mod, so recipe load conditions can't see it. Without the pack, RemovedRecipes drops every recipe
 * for these, and the creative tab and JEI hide them.
 */
public final class ArmorerCompat {
    public static final String NAMESPACE = "immersive_armorer";
    public static final String CELLS_BLUEPRINT = "energy_cells";
    // the pack's blueprints: its cartridges' molds and projectiles, and the capacitor cells
    private static final Set<String> BLUEPRINTS = Set.of(CELLS_BLUEPRINT, "immersive_armorer_casing_molds", "immersive_armorer_projectiles");
    private static final ResourceLocation PROBE = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "454_casul");

    private static Set<Item> items;

    /**
     * Whether the gun pack is loaded: on the client from TaCZ's client index (the player's own gun packs), otherwise
     * from the server's. A dedicated server always asks its own.
     */
    public static boolean packLoaded(boolean client) {
        return client && FMLEnvironment.dist == Dist.CLIENT ? TimelessAPI.getClientAmmoIndex(PROBE).isPresent() : TimelessAPI.getCommonAmmoIndex(PROBE).isPresent();
    }

    /** Our casings, projectiles and molds for the pack's calibers. */
    public static Set<Item> items() {
        if (items == null) {
            Set<Item> set = new HashSet<>();
            for (String caliber : AmmoComponentItems.ARMORER_CALIBERS)
                for (Map<String, RegistryObject<Item>> map : List.of(
                        AmmoComponentItems.EMPTY_CASINGS, AmmoComponentItems.PROJECTILES, AmmoComponentItems.CASING_MOLDS))
                    set.add(map.get(caliber).get());
            items = set;
        }
        return items;
    }

    /** Anything that only exists for the pack: our parts for it, its ammo, and its blueprints. */
    public static boolean isArmorerStack(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (items().contains(stack.getItem())) return true;
        if (stack.getItem() instanceof IAmmo ammo) return NAMESPACE.equals(ammo.getAmmoId(stack).getNamespace());
        CompoundTag tag = stack.getTag();
        return stack.getItem() == IEItems.Misc.BLUEPRINT.asItem() && tag != null && BLUEPRINTS.contains(tag.getString("blueprint"));
    }

    private ArmorerCompat() {}
}
