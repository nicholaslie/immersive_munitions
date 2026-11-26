package dev.immersive_munitions.compat;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.immersive_munitions.Config;
import dev.immersive_munitions.ImmersiveMunitions;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

/**
 * Keeps the removed IE guns and their ammo (RemovedRecipes) out of chest loot and villager trades, while
 * {@code remove_ie_weapons} is on: IE's engineer's house chests hold the bullet blueprints, and the Gunsmith sells
 * bullets, blueprints and revolver parts and buys empty casings.
 */
public final class RemovedWeaponLoot {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ImmersiveMunitions.MODID);
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<Modifier>> REMOVE_IE_WEAPONS =
            LOOT_MODIFIERS.register("remove_ie_weapons", () -> Modifier.CODEC);

    /** Drops the removed IE items from any generated loot (data/neoforge/loot_modifiers/global_loot_modifiers.json). */
    public static final class Modifier extends LootModifier {
        static final MapCodec<Modifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, Modifier::new));

        Modifier(LootItemCondition[] conditions) {
            super(conditions);
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
            if (Config.REMOVE_IE_WEAPONS.get()) loot.removeIf(RemovedRecipes::isIeWeaponStack);
            return loot;
        }

        @Override
        public MapCodec<? extends IGlobalLootModifier> codec() {
            return REMOVE_IE_WEAPONS.get();
        }
    }

    /**
     * Wraps every villager trade so an offer that sells or asks for a removed IE item is skipped (a null offer isn't
     * added). Runs after IE has added its Gunsmith trades; the config is checked when the offer is made. The Gunsmith
     * itself is reworked first (GunsmithTrades).
     */
    @EventBusSubscriber(modid = ImmersiveMunitions.MODID)
    public static final class Trades {
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onVillagerTrades(VillagerTradesEvent event) {
            var profession = BuiltInRegistries.VILLAGER_PROFESSION.getKey(event.getType());
            if (GunsmithTrades.GUNSMITH.equals(profession)) GunsmithTrades.rework(event.getTrades(), Config.REMOVE_IE_WEAPONS.get());
            event.getTrades().values().forEach(Trades::wrap);
        }

        private static void wrap(List<net.minecraft.world.entity.npc.VillagerTrades.ItemListing> listings) {
            listings.replaceAll(listing -> (trader, random) -> {
                MerchantOffer offer = listing.getOffer(trader, random);
                if (offer == null || !Config.REMOVE_IE_WEAPONS.get()) return offer;
                boolean weapon = RemovedRecipes.isIeWeaponStack(offer.getResult())
                        || RemovedRecipes.isIeWeaponStack(offer.getBaseCostA()) || RemovedRecipes.isIeWeaponStack(offer.getCostB());
                return weapon ? null : offer;
            });
        }
    }

    private RemovedWeaponLoot() {}
}
