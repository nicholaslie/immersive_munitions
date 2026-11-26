package dev.immersive_munitions.tacz;

import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Creates TaCZ ammo item stacks through TaCZ's API. */
public final class TaczAmmoStacks {

    /** A real TaCZ ammo stack, e.g. create("9mm", 30); a bare id is TaCZ's, or give the namespace ("immersive_armorer:20mm"). */
    public static ItemStack create(String ammoId, int count) {
        ResourceLocation id = ammoId.contains(":") ? ResourceLocation.tryParse(ammoId) : ResourceLocation.fromNamespaceAndPath("tacz", ammoId);
        return AmmoItemBuilder.create()
                .setId(id)
                .setCount(count)
                .build();
    }

    private TaczAmmoStacks() {}
}
