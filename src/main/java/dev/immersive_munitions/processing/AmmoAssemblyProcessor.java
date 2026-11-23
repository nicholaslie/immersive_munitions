package dev.immersive_munitions.processing;

import dev.immersive_munitions.registry.AmmoComponentItems;
import dev.immersive_munitions.tacz.TaczAmmoStacks;
import com.tacz.guns.api.TimelessAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Transaction-safe core of the two-stage ammunition process.
 * The future multiblock tick handler calls this only after reserving an output slot.
 */
public final class AmmoAssemblyProcessor {
    private AmmoAssemblyProcessor() {}

    public static boolean canAssemble(String ammoId, ItemStack casing, ItemStack projectile, ItemStack gunpowder) {
        if (ammoId == null || !AmmoComponentItems.EMPTY_CASINGS.containsKey(ammoId)
                || !AmmoComponentItems.PROJECTILES.containsKey(ammoId)
                || TimelessAPI.getCommonAmmoIndex(new ResourceLocation("tacz", ammoId)).isEmpty()) {
            return false;
        }
        boolean standardCasing = casing.is(AmmoComponentItems.EMPTY_CASINGS.get(ammoId).get());
        return standardCasing
                && projectile.is(AmmoComponentItems.PROJECTILES.get(ammoId).get())
                && gunpowder.is(Items.GUNPOWDER)
                && !casing.isEmpty() && !projectile.isEmpty() && !gunpowder.isEmpty();
    }

    /** Consumes exactly one casing, projectile, and gunpowder only when all inputs match. */
    public static ItemStack assemble(String ammoId, ItemStack casing, ItemStack projectile, ItemStack gunpowder) {
        if (!canAssemble(ammoId, casing, projectile, gunpowder)) {
            return ItemStack.EMPTY;
        }
        ItemStack output = TaczAmmoStacks.create(ammoId, 1);
        if (output.isEmpty()) {
            return ItemStack.EMPTY;
        }
        casing.shrink(1);
        projectile.shrink(1);
        gunpowder.shrink(1);
        return output;
    }
}
