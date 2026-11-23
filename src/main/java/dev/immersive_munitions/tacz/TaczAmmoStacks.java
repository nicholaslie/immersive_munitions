package dev.immersive_munitions.tacz;

import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class TaczAmmoStacks {

    /** A real TaCZ ammo stack, e.g. create("9mm", 30). */
    public static ItemStack create(String ammoId, int count) {
        return AmmoItemBuilder.create()
                .setId(ResourceLocation.fromNamespaceAndPath("tacz", ammoId))
                .setCount(count)
                .build();
    }

    private TaczAmmoStacks() {}
}
