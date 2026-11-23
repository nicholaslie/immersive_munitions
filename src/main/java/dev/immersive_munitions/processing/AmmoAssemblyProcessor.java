package dev.immersive_munitions.processing;

import com.tacz.guns.api.TimelessAPI;
import dev.immersive_munitions.registry.AmmoComponentItems;
import dev.immersive_munitions.tacz.TaczAmmoStacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.RegistryObject;

/**
 * Casing + projectile + gunpowder -> one TaCZ round.
 * Not wired to a machine yet; the assembly multiblock will call this once it reserves an output slot.
 */
public final class AmmoAssemblyProcessor {

    public static boolean canAssemble(String ammoId, ItemStack casing, ItemStack projectile, ItemStack gunpowder) {
        RegistryObject<Item> casingItem = AmmoComponentItems.EMPTY_CASINGS.get(ammoId);
        RegistryObject<Item> projectileItem = AmmoComponentItems.PROJECTILES.get(ammoId);
        if (casingItem == null || projectileItem == null) return false;
        if (TimelessAPI.getCommonAmmoIndex(ResourceLocation.fromNamespaceAndPath("tacz", ammoId)).isEmpty()) return false;

        return casing.is(casingItem.get())
                && projectile.is(projectileItem.get())
                && gunpowder.is(Items.GUNPOWDER);
    }

    /** Uses up one of each input and returns the round, or returns EMPTY and leaves the inputs alone. */
    public static ItemStack assemble(String ammoId, ItemStack casing, ItemStack projectile, ItemStack gunpowder) {
        if (!canAssemble(ammoId, casing, projectile, gunpowder)) return ItemStack.EMPTY;

        ItemStack round = TaczAmmoStacks.create(ammoId, 1);
        if (round.isEmpty()) return ItemStack.EMPTY;

        casing.shrink(1);
        projectile.shrink(1);
        gunpowder.shrink(1);
        return round;
    }

    private AmmoAssemblyProcessor() {}
}
