package dev.immersive_munitions.compat;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.common.register.IEItems;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import dev.immersive_munitions.Config;
import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Switches off other mods' recipes so ammunition comes from the Ammunition Assembler (each part has a config switch,
 * both on by default): the gun smith table's ammo recipes, and Immersive Engineering's guns (revolver, railgun,
 * chemthrower, both turrets) with their ammo and the parts and upgrades only they use.
 *
 * Recipes are removed by what they make, so other mods' recipes for these items go too. The filtering runs where TaCZ
 * finishes its own recipes: the RecipeManager is captured when server data reloads, and filtered in TagsUpdatedEvent,
 * which fires after the reload and before recipes are sent to players. IE rebuilds its recipe caches on the same event.
 * The removed IE items stay registered (old worlds keep them) but are hidden from IE's creative tab and from JEI.
 */
@Mod.EventBusSubscriber(modid = ImmersiveMunitions.MODID)
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
        if (!tacz && !ie) return;
        RegistryAccess access = event.getRegistryAccess();
        List<Recipe<?>> kept = new ArrayList<>();
        int removed = 0;
        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            if ((tacz && isTaczAmmo(recipe)) || (ie && makesIeWeaponItem(recipe, access))) removed++;
            else kept.add(recipe);
        }
        if (removed > 0) {
            recipeManager.replaceRecipes(kept);
            LOG.info("Removed {} recipes (TaCZ ammo: {}, IE guns: {})", removed, tacz, ie);
        }
        recipeManager = null;
    }

    private static boolean isTaczAmmo(Recipe<?> recipe) {
        if (!(recipe instanceof GunSmithTableRecipe table)) return false;
        table.init();   // resolves the result, as TaCZ does on this same event; safe to repeat
        return table.getOutput().getItem() instanceof IAmmo;
    }

    private static boolean makesIeWeaponItem(Recipe<?> recipe, RegistryAccess access) {
        try {
            if (recipe instanceof MultiblockRecipe multiblock) {
                for (ItemStack out : multiblock.getItemOutputs()) if (isIeWeaponStack(out)) return true;
                return false;
            }
            return isIeWeaponStack(recipe.getResultItem(access));
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
        CompoundTag tag = stack.getTag();
        return stack.getItem() == IEItems.Misc.BLUEPRINT.asItem() && tag != null && BULLET_BLUEPRINTS.contains(tag.getString("blueprint"));
    }

    private static final Set<String> BULLET_BLUEPRINTS = Set.of("bullet", "specialBullet");

    public static Set<Item> ieWeaponItems() {
        if (ieWeaponItems == null) {
            Set<Item> set = new HashSet<>();
            for (String id : IE_WEAPON_ITEMS) {
                Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(IE, id));
                if (item != null && ForgeRegistries.ITEMS.getKey(item).getNamespace().equals(IE)) set.add(item);
                else LOG.warn("Immersive Engineering item {} not found", id);
            }
            IEItems.Weapons.BULLETS.values().forEach(bullet -> set.add(bullet.asItem()));
            ieWeaponItems = set;
        }
        return ieWeaponItems;
    }

    /** Takes the removed IE items out of IE's creative tab. */
    @Mod.EventBusSubscriber(modid = ImmersiveMunitions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class CreativeTab {
        @SubscribeEvent
        public static void onBuildTab(BuildCreativeModeTabContentsEvent event) {
            if (!event.getTabKey().equals(IE_TAB) || !Config.REMOVE_IE_WEAPONS.get()) return;
            List<ItemStack> hidden = new ArrayList<>();
            for (var entry : event.getEntries()) if (isIeWeaponStack(entry.getKey())) hidden.add(entry.getKey());
            for (ItemStack stack : hidden) event.getEntries().remove(stack);
        }
    }

    private RemovedRecipes() {}
}
