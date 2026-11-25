package dev.immersive_munitions.registry;

import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** Item tags the mod checks. */
public final class ModTags {
    /** Anything the assembler's projectile hopper accepts. */
    public static final TagKey<Item> PROJECTILES = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ImmersiveMunitions.MODID, "projectiles"));

    private ModTags() {}
}
