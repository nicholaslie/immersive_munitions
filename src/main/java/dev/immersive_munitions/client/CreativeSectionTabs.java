package dev.immersive_munitions.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.immersive_munitions.ImmersiveMunitions;
import dev.immersive_munitions.registry.ModTabs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/** TaCZ-style category tabs for the Immersive Munitions creative inventory. */
@Mod.EventBusSubscriber(modid = ImmersiveMunitions.MODID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CreativeSectionTabs {
    // TaCZ uses the vanilla recipe-book tab sheet for its creative-pack sidebar.
    private static final ResourceLocation TAB_TEXTURE = new ResourceLocation("textures/gui/recipe_book.png");
    private static final List<SectionButton> BUTTONS = new ArrayList<>();

    @SubscribeEvent
    public static void initializeGui(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof CreativeModeInventoryScreen screen)) return;

        BUTTONS.clear();
        ModTabs.Section[] sections = ModTabs.Section.values();
        int x = screen.getGuiLeft() - 28;
        int y = screen.getGuiTop() + 11;
        int spacing = 29;
        boolean show = isMunitionsTabSelected();

        for (int i = 0; i < sections.length; i++) {
            ModTabs.Section section = sections[i];
            SectionButton button = new SectionButton(x, y + i * spacing, section,
                    pressed -> select(screen, section));
            button.visible = show;
            button.active = show && section != ModTabs.selectedSection;
            BUTTONS.add(button);
            event.addListener(button);
        }
    }

    @SubscribeEvent
    public static void updateVisibility(ScreenEvent.BackgroundRendered event) {
        if (!(event.getScreen() instanceof CreativeModeInventoryScreen)) return;

        // Resolve visibility continuously instead of relying on a one-time flag.
        // This also handles reopening the creative screen after Escape.
        boolean show = isMunitionsTabSelected();
        for (SectionButton button : BUTTONS) {
            button.visible = show;
            button.active = show && button.section != ModTabs.selectedSection;
        }
    }

    private static boolean isMunitionsTabSelected() {
        return CreativeModeInventoryScreen.selectedTab == ModTabs.MAIN.get();
    }

    private static void select(CreativeModeInventoryScreen screen, ModTabs.Section section) {
        if (ModTabs.selectedSection == section) return;
        ModTabs.selectSection(section);
        screen.resize(Minecraft.getInstance(), screen.width, screen.height);
    }

    private static final class SectionButton extends Button {
        private final ModTabs.Section section;
        private final ItemStack icon;

        private SectionButton(int x, int y, ModTabs.Section section, OnPress onPress) {
            super(x, y, 32, 26, Component.empty(), onPress, DEFAULT_NARRATION);
            this.section = section;
            this.icon = new ItemStack(section.icon.get());
            this.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                    Component.translatable("itemGroup." + ImmersiveMunitions.MODID + ".section." + section.id)));
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (!visible) return;
            boolean selected = section == ModTabs.selectedSection;
            RenderSystem.enableBlend();

            // Match TaCZ's own creative sidebar tab sprite and UV selection.
            // TaCZ draws the selected tab with the raised/darker variant.
            graphics.blitNineSliced(TAB_TEXTURE, getX(), getY(), selected ? 31 : 28, 26,
                    3, selected ? 35 : 30, 26, selected ? 188 : 153, 2);
            graphics.renderItem(icon, getX() + 8, getY() + 5);

            if (isHovered && Minecraft.getInstance().options.touchscreen().get()) {
                graphics.fill(getX() + 5, getY() + 3, getX() + 27, getY() + 23, 0x80FFFFFF);
            }
        }
    }

    private CreativeSectionTabs() {}
}
