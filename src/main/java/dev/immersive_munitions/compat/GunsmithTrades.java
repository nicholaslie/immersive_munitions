package dev.immersive_munitions.compat;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.register.IEItems;
import dev.immersive_munitions.registry.ModItems;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.Supplier;

/**
 * IE's Gunsmith villager, reworked around this mod: with IE's guns removed (RemovedRecipes) its bullet, blueprint and
 * revolver part trades are taken out of the pool, and it sells our casing mold and projectile blueprints instead, one
 * gun type per level, and buys brass, gunpowder and lead. IE's wooden grip trade stays. Like any villager, it gets two
 * offers per level, picked from that level's list.
 *
 *   1 Novice      buys brass ingots, gunpowder (plus IE's wooden grip)
 *   2 Apprentice  pistol and shotgun blueprints
 *   3 Journeyman  rifle blueprints; buys lead ingots
 *   4 Expert      heavy blueprints
 *   5 Master      grenade blueprints
 */
public final class GunsmithTrades {
    public static final ResourceLocation GUNSMITH = ResourceLocation.fromNamespaceAndPath("immersiveengineering", "gunsmith");

    /** Called with the Gunsmith's trade lists, after IE has filled them. */
    public static void rework(Int2ObjectMap<List<ItemListing>> trades, boolean removeIeWeapons) {
        if (removeIeWeapons) trades.values().forEach(level -> level.removeIf(GunsmithTrades::isIeWeaponTrade));

        trades.get(1).add(buy(GunsmithTrades::brassIngot, 6, 10, 16, 2));
        trades.get(1).add(buy(() -> Items.GUNPOWDER, 8, 12, 16, 2));

        trades.get(2).add(blueprint("pistol_casing_molds", 3, 6, 25));
        trades.get(2).add(blueprint("pistol_projectiles", 3, 6, 25));
        trades.get(2).add(blueprint("shotgun_casing_molds", 3, 6, 25));
        trades.get(2).add(blueprint("shotgun_projectiles", 3, 6, 25));

        trades.get(3).add(blueprint("rifle_casing_molds", 5, 9, 30));
        trades.get(3).add(blueprint("rifle_projectiles", 5, 9, 30));
        trades.get(3).add(buy(GunsmithTrades::leadIngot, 4, 8, 16, 10));

        trades.get(4).add(blueprint("heavy_casing_molds", 8, 14, 30));
        trades.get(4).add(blueprint("heavy_projectiles", 8, 14, 30));

        trades.get(5).add(blueprint("grenade_casing_molds", 10, 16, 30));
        trades.get(5).add(blueprint("grenade_projectiles", 10, 16, 30));
    }

    /**
     * Whether one of IE's listings deals in a removed item. IE's listings don't need the villager to build an offer,
     * so a sample offer shows what it trades; a listing that can't be sampled stays (RemovedWeaponLoot still screens
     * its real offers).
     */
    private static boolean isIeWeaponTrade(ItemListing listing) {
        try {
            MerchantOffer offer = listing.getOffer(null, RandomSource.create(0));
            return offer != null && (RemovedRecipes.isIeWeaponStack(offer.getResult())
                    || RemovedRecipes.isIeWeaponStack(offer.getBaseCostA()) || RemovedRecipes.isIeWeaponStack(offer.getCostB()));
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Sells one of our blueprints for a random price in [min, max] emeralds; one per restock, like IE's. */
    private static ItemListing blueprint(String category, int min, int max, int xp) {
        return (trader, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, min + random.nextInt(max - min + 1)),
                BlueprintCraftingRecipe.getTypedBlueprint(category), 1, xp, 0.2f);
    }

    /** Buys [min, max] of an item for one emerald. */
    private static ItemListing buy(Supplier<Item> item, int min, int max, int uses, int xp) {
        return (trader, random) -> new MerchantOffer(
                new ItemStack(item.get(), min + random.nextInt(max - min + 1)),
                new ItemStack(Items.EMERALD), uses, xp, 0.05f);
    }

    private static Item leadIngot() {
        return IEItems.Metals.INGOTS.get(EnumMetals.LEAD).asItem();
    }

    /** Our brass ingot, or Create's when ours is hidden for it (MaterialCompat). */
    private static Item brassIngot() {
        Item ours = ModItems.BRASS_INGOT.get();
        if (!MaterialCompat.isHidden(ours)) return ours;
        Item create = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(MaterialCompat.CREATE, "brass_ingot"));
        return create == null || create == Items.AIR ? ours : create;
    }

    private GunsmithTrades() {}
}
