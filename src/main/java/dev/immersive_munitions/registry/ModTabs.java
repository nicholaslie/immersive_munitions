package dev.immersive_munitions.registry;

import dev.immersive_munitions.ImmersiveMunitions;
import dev.immersive_munitions.compat.ArmorerCompat;
import dev.immersive_munitions.compat.MaterialCompat;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * One creative tab split into sections. The tab only shows the selected section;
 * the section buttons on its left side are in client/CreativeSectionTabs.
 */
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

    public static Section selectedSection = Section.SHELLS;

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () ->
            new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup." + ImmersiveMunitions.MODID))
                    .icon(() -> new ItemStack(CreativeIconItems.EMPTY_CASING.get()))
                    .displayItems((params, output) -> allItems().forEach(output::accept))
                    .withTabFactory(SectionedTab::new)
                    .build());

    public static void selectSection(Section section) {
        if (selectedSection == section) return;
        selectedSection = section;
        SectionedTab.dirty = true;
    }

    private static List<ItemStack> allItems() {
        List<ItemStack> stacks = new ArrayList<>();
        for (Section section : Section.values()) addItems(section, stacks);
        return stacks;
    }

    private static void addItems(Section section, Collection<ItemStack> out) {
        switch (section) {
            // ModItems also holds the block items, so this covers the ore and storage blocks. Leaves out what Create or
            // Immersive Geology already provide (MaterialCompat)
            case MATERIALS -> ModItems.ITEMS.getEntries().stream().map(RegistryObject::get)
                    .filter(item -> !MaterialCompat.isHidden(item)).forEach(item -> out.add(new ItemStack(item)));
            case MOLDS -> addParts(AmmoComponentItems.CASING_MOLDS, out);
            case SHELLS -> addParts(AmmoComponentItems.EMPTY_CASINGS, out);
            case PROJECTILES -> addParts(AmmoComponentItems.PROJECTILES, out);
        }
    }

    // a gun pack's parts only show when that pack is installed (ArmorerCompat)
    private static void addParts(Map<String, RegistryObject<Item>> parts, Collection<ItemStack> out) {
        boolean armorer = ArmorerCompat.packLoaded(true);
        parts.forEach((caliber, item) -> {
            if (armorer || !AmmoComponentItems.ARMORER_CALIBERS.contains(caliber)) out.add(new ItemStack(item.get()));
        });
    }

    /** Shows only the selected section; search still sees everything. */
    private static class SectionedTab extends CreativeModeTab {
        private static final List<ItemStack> shown = new ArrayList<>();
        private static boolean dirty = true;

        private SectionedTab(CreativeModeTab.Builder builder) {
            super(builder);
        }

        @Override
        public Collection<ItemStack> getDisplayItems() {
            if (dirty) {
                shown.clear();
                addItems(selectedSection, shown);
                dirty = false;
            }
            return shown;
        }

        @Override
        public Collection<ItemStack> getSearchTabDisplayItems() {
            return allItems();
        }
    }

    private ModTabs() {}
}
