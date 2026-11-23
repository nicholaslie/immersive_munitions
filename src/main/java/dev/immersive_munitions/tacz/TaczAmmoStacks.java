package dev.immersive_munitions.tacz;

import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Factory for real TaCZ ammunition stacks, retaining the pack ammo ID. */
public final class TaczAmmoStacks {
    private TaczAmmoStacks() {}

    public static ItemStack create(String ammoId, int count) {
        return AmmoItemBuilder.create()
                .setId(new ResourceLocation("tacz", ammoId))
                .setCount(count)
                .build();
    }
}
