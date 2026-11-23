package dev.immersive_munitions.registry;

import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/** One public creative tab with four ordered subcategories. */
public final class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ImmersiveMunitions.MODID);

    public enum Section {
        MATERIALS("brass_zinc", CreativeIconItems.MATERIALS),
        MOLDS("molds", CreativeIconItems.MOLD),
        SHELLS("shells", CreativeIconItems.EMPTY_CASING),
        PROJECTILES("projectiles", CreativeIconItems.PROJECTILE);

        public final String id;
        public final RegistryObject<Item> icon;

        Section(String id, RegistryObject<Item> icon) {
            this.id = id;
            this.icon = icon;
        }
    }

    public static volatile Section selectedSection = Section.SHELLS;

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () ->
            new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup." + ImmersiveMunitions.MODID))
                    .icon(() -> new ItemStack(CreativeIconItems.EMPTY_CASING.get()))
                    .displayItems(MunitionsCreativeTab::fillAllItems)
                    .withTabFactory(MunitionsCreativeTab::new)
                    .build());

    private static class MunitionsCreativeTab extends CreativeModeTab {
        private static final ArrayList<ItemStack> DISPLAY_CACHE = new ArrayList<>();
        private static boolean refreshDisplay = true;

        private MunitionsCreativeTab(CreativeModeTab.Builder builder) {
            super(builder);
            // Match the robust initialization pattern used by the Geology tab:
            // populate the tab's all-items backing collection when constructed,
            // so its first opening works even if no other addon tab was opened.
            DISPLAY_CACHE.addAll(getSearchTabDisplayItems());
        }

        private static void fillAllItems(CreativeModeTab.ItemDisplayParameters parameters,
                                         CreativeModeTab.Output output) {
            ArrayList<ItemStack> all = new ArrayList<>();
            for (Section section : Section.values()) addSectionStacks(section, all);
            all.forEach(output::accept);
        }

        private static void invalidateDisplay() {
            refreshDisplay = true;
            DISPLAY_CACHE.clear();
        }

        @Override
        public Collection<ItemStack> getDisplayItems() {
            if (refreshDisplay) {
                DISPLAY_CACHE.clear();
                addSectionStacks(selectedSection, DISPLAY_CACHE);
                refreshDisplay = false;
            }
            return DISPLAY_CACHE;
        }

        @Override
        public Collection<ItemStack> getSearchTabDisplayItems() {
            ArrayList<ItemStack> stacks = new ArrayList<>();
            for (Section section : Section.values()) addSectionStacks(section, stacks);
            return stacks;
        }
    }

    public static void selectSection(Section section) {
        if (selectedSection != section) {
            selectedSection = section;
            MunitionsCreativeTab.invalidateDisplay();
        }
    }

    private static void addSectionStacks(Section section, Collection<ItemStack> output) {
        switch (section) {
            case SHELLS -> addAll(AmmoComponentItems.EMPTY_CASINGS, output);
            case MOLDS -> addAll(AmmoComponentItems.CASING_MOLDS, output);
            case PROJECTILES -> addAll(AmmoComponentItems.PROJECTILES, output);
            case MATERIALS -> {
                ModItems.ITEMS.getEntries().forEach(item -> output.add(new ItemStack(item.get())));
                ModBlocks.BLOCKS.getEntries().forEach(block -> output.add(new ItemStack(block.get())));
            }
        }
    }

    private static void addAll(Map<String, RegistryObject<Item>> items, Collection<ItemStack> output) {
        items.values().forEach(item -> output.add(new ItemStack(item.get())));
    }

    private ModTabs() {}
}
