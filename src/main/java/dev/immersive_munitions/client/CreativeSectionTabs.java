package dev.immersive_munitions.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.immersive_munitions.ImmersiveMunitions;
import dev.immersive_munitions.registry.ModTabs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayList;
import java.util.List;

/**
 * Section buttons down the left side of the creative inventory while our tab is open.
 * Layout and tab sprites follow TaCZ's creative sidebar (GPLv3, see NOTICE.md).
 */
@EventBusSubscriber(modid = ImmersiveMunitions.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class CreativeSectionTabs {
    // the recipe book's tab sprites
    private static final ResourceLocation TAB = ResourceLocation.withDefaultNamespace("recipe_book/tab");
    private static final ResourceLocation TAB_SELECTED = ResourceLocation.withDefaultNamespace("recipe_book/tab_selected");
    private static final int SPACING = 29;
    private static final List<SectionButton> buttons = new ArrayList<>();

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof CreativeModeInventoryScreen screen)) return;

        buttons.clear();
        int x = screen.getGuiLeft() - 28;
        int y = screen.getGuiTop() + 11;
        for (ModTabs.Section section : ModTabs.Section.values()) {
            SectionButton button = new SectionButton(x, y, section, b -> select(screen, section));
            buttons.add(button);
            event.addListener(button);
            y += SPACING;
        }
        refreshButtons();
    }

    // Checked at the start of every frame (where TaCZ hooks its sidebar) so the buttons follow tab switches and
    // reopening the screen
    @SubscribeEvent
    public static void onRender(ScreenEvent.Render.Pre event) {
        if (event.getScreen() instanceof CreativeModeInventoryScreen) refreshButtons();
    }

    private static void refreshButtons() {
        boolean ourTabOpen = CreativeModeInventoryScreen.selectedTab == ModTabs.MAIN.get();
        for (SectionButton button : buttons) {
            button.visible = ourTabOpen;
            button.active = ourTabOpen && button.section != ModTabs.selectedSection;
        }
    }

    private static void select(CreativeModeInventoryScreen screen, ModTabs.Section section) {
        if (ModTabs.selectedSection == section) return;
        ModTabs.selectSection(section);
        screen.resize(Minecraft.getInstance(), screen.width, screen.height);  // rebuilds the item grid
    }

    private static final class SectionButton extends Button {
        private final ModTabs.Section section;
        private final ItemStack icon;

        SectionButton(int x, int y, ModTabs.Section section, OnPress onPress) {
            super(x, y, 32, 26, Component.empty(), onPress, DEFAULT_NARRATION);
            this.section = section;
            this.icon = new ItemStack(section.icon.get());
            setTooltip(Tooltip.create(Component.translatable(
                    "itemGroup." + ImmersiveMunitions.MODID + ".section." + section.id)));
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (!visible) return;
            boolean selected = section == ModTabs.selectedSection;

            RenderSystem.enableBlend();
            // Recipe book tab sprites, as TaCZ draws them: both are 35x27, the unselected one with five transparent
            // columns on the right. Crop only the flat centre to keep the borders and the selected tab's join to the panel
            ResourceLocation sprite = selected ? TAB_SELECTED : TAB;
            int width = selected ? 31 : 28, sourceWidth = selected ? 35 : 30;
            graphics.blitSprite(sprite, 35, 27, 0, 0, getX(), getY(), width - 3, 26);
            graphics.blitSprite(sprite, 35, 27, sourceWidth - 3, 0, getX() + width - 3, getY(), 3, 26);
            graphics.renderItem(icon, getX() + 8, getY() + 5);

            if (isHovered && Minecraft.getInstance().options.touchscreen().get()) {
                graphics.fill(getX() + 5, getY() + 3, getX() + 27, getY() + 23, 0x80FFFFFF);
            }
        }
    }

    private CreativeSectionTabs() {}
}
