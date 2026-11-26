package dev.immersive_munitions.compat;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.common.register.IEItems;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import dev.immersive_munitions.Config;
import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import blusunrize.immersiveengineering.api.IEApiDataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Switches off other mods' recipes so ammunition comes from the Ammunition Assembler (each part has a config switch,
 * both on by default): the gun smith table's ammo recipes, and Immersive Engineering's guns (revolver, railgun,
 * chemthrower, both turrets) with their ammo and the parts and upgrades only they use.
 *
 * Recipes are removed by what they make, so other mods' recipes for these items go too. The filtering runs where TaCZ
 * finishes its own recipes: the RecipeManager is captured when server data reloads, and filtered in TagsUpdatedEvent,
 * which fires after the reload and before recipes are sent to players. IE rebuilds its recipe caches on the same event.
 * The removed IE items stay registered (old worlds keep them) but are hidden from IE's creative tab and from JEI.
 * Without the Immersive Armorer gun pack, every recipe for its ammo and our parts for it goes too (ArmorerCompat).
 */
@EventBusSubscriber(modid = ImmersiveMunitions.MODID)
public final class RemovedRecipes {
    private static final Logger LOG = LogUtils.getLogger();
    private static final String IE = "immersiveengineering";

    /** IE items that only the removed guns use; the bullets are added from IE's own list. */
    private static final List<String> IE_WEAPON_ITEMS = List.of(
            "revolver", "speedloader", "railgun", "chemthrower", "turret_gun", "turret_chem",
            "gunpart_barrel", "gunpart_drum", "gunpart_hammer",
            "empty_casing", "empty_shell", "mold_bullet_casing",
            "toolupgrade_revolver_bayonet", "toolupgrade_revolver_magazine", "toolupgrade_revolver_electro",
            "toolupgrade_railgun_scope", "toolupgrade_railgun_capacitors",
            "toolupgrade_chemthrower_focus", "toolupgrade_chemthrower_multitank");
    private static final ResourceKey<CreativeModeTab> IE_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(IE, "main"));

    private static RecipeManager recipeManager;
    private static Set<Item> ieWeaponItems;

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        recipeManager = event.getServerResources().getRecipeManager();
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD || recipeManager == null) return;
        boolean tacz = Config.REMOVE_TACZ_AMMO.get(), ie = Config.REMOVE_IE_WEAPONS.get();
        boolean noArmorer = !ArmorerCompat.packLoaded(false);   // without the gun pack, our recipes for it go too
        if (!tacz && !ie && !noArmorer) return;
        RegistryAccess access = event.getRegistryAccess();
        List<RecipeHolder<?>> kept = new ArrayList<>();
        int removed = 0;
        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            Recipe<?> recipe = holder.value();
            if ((tacz && isTaczAmmo(recipe, access)) || (ie && makes(recipe, access, RemovedRecipes::isIeWeaponStack))
                    || (noArmorer && makes(recipe, access, ArmorerCompat::isArmorerStack))) removed++;
            else kept.add(holder);
        }
        if (removed > 0) {
            recipeManager.replaceRecipes(kept);
            LOG.info("Removed {} recipes (TaCZ ammo: {}, IE guns: {}, Immersive Armorer missing: {})", removed, tacz, ie, noArmorer);
        }
        recipeManager = null;
    }

    private static boolean isTaczAmmo(Recipe<?> recipe, RegistryAccess access) {
        if (!(recipe instanceof GunSmithTableRecipe table)) return false;
        table.init(access);   // resolves the result, as TaCZ does on this same event; safe to repeat
        return table.getOutput().getItem() instanceof IAmmo;
    }

    /** Whether a recipe's output (any of them, for IE's machines) matches. */
    private static boolean makes(Recipe<?> recipe, RegistryAccess access, Predicate<ItemStack> match) {
        try {
            if (recipe instanceof MultiblockRecipe multiblock) {
                for (ItemStack out : multiblock.getItemOutputs()) if (match.test(out)) return true;
                return false;
            }
            return match.test(recipe.getResultItem(access));
        } catch (RuntimeException e) {   // a recipe whose output can't be resolved here is left alone
            return false;
        }
    }

    /**
     * Whether a stack is one of the removed IE items, including both bullet blueprints: "bullet" (crafted) and
     * "specialBullet" (only from the Gunsmith and engineer's house chests).
     */
    public static boolean isIeWeaponStack(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (ieWeaponItems().contains(stack.getItem())) return true;
        return stack.getItem() == IEItems.Misc.BLUEPRINT.asItem() && BULLET_BLUEPRINTS.contains(IEApiDataComponents.getBlueprintType(stack));
    }

    private static final Set<String> BULLET_BLUEPRINTS = Set.of("bullet", "specialBullet");

    public static Set<Item> ieWeaponItems() {
        if (ieWeaponItems == null) {
            Set<Item> set = new HashSet<>();
            for (String id : IE_WEAPON_ITEMS) {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(IE, id));
                if (item != null && BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(IE)) set.add(item);
                else LOG.warn("Immersive Engineering item {} not found", id);
            }
            IEItems.Weapons.BULLETS.values().forEach(bullet -> set.add(bullet.asItem()));
            ieWeaponItems = set;
        }
        return ieWeaponItems;
    }

    /** Takes the removed IE items out of IE's creative tab. */
    @EventBusSubscriber(modid = ImmersiveMunitions.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static final class CreativeTab {
        @SubscribeEvent
        public static void onBuildTab(BuildCreativeModeTabContentsEvent event) {
            if (!event.getTabKey().equals(IE_TAB) || !Config.REMOVE_IE_WEAPONS.get()) return;
            List<ItemStack> hidden = new ArrayList<>();
            for (ItemStack stack : event.getParentEntries()) if (isIeWeaponStack(stack)) hidden.add(stack);
            for (ItemStack stack : event.getSearchEntries()) if (isIeWeaponStack(stack) && !hidden.contains(stack)) hidden.add(stack);
            for (ItemStack stack : hidden) event.remove(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    private RemovedRecipes() {}
}
