package dev.immersive_munitions.compat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.immersive_munitions.Config;
import dev.immersive_munitions.ImmersiveMunitions;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/**
 * Keeps the removed IE guns and their ammo (RemovedRecipes) out of chest loot and villager trades, while
 * {@code remove_ie_weapons} is on: IE's engineer's house chests hold the bullet blueprints, and the Gunsmith sells
 * bullets, blueprints and revolver parts and buys empty casings.
 */
public final class RemovedWeaponLoot {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ImmersiveMunitions.MODID);
    public static final RegistryObject<Codec<Modifier>> REMOVE_IE_WEAPONS =
            LOOT_MODIFIERS.register("remove_ie_weapons", () -> Modifier.CODEC);

    /** Drops the removed IE items from any generated loot (data/forge/loot_modifiers/global_loot_modifiers.json). */
    public static final class Modifier extends LootModifier {
        static final Codec<Modifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, Modifier::new));

        Modifier(LootItemCondition[] conditions) {
            super(conditions);
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
            if (Config.REMOVE_IE_WEAPONS.get()) loot.removeIf(RemovedRecipes::isIeWeaponStack);
            return loot;
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return REMOVE_IE_WEAPONS.get();
        }
    }

    /**
     * Wraps every villager trade so an offer that sells or asks for a removed IE item is skipped (a null offer isn't
     * added). Runs after IE has added its Gunsmith trades; the config is checked when the offer is made. The Gunsmith
     * itself is reworked first (GunsmithTrades).
     */
    @Mod.EventBusSubscriber(modid = ImmersiveMunitions.MODID)
    public static final class Trades {
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onVillagerTrades(VillagerTradesEvent event) {
            var profession = ForgeRegistries.VILLAGER_PROFESSIONS.getKey(event.getType());
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
