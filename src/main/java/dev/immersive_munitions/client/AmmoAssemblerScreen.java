package dev.immersive_munitions.client;

import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import com.google.common.collect.ImmutableList;
import dev.immersive_munitions.ImmersiveMunitions;
import dev.immersive_munitions.multiblock.AmmoAssemblerMenu;
import dev.immersive_munitions.registry.ModMenus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

/**
 * The Ammunition Assembler's GUI screen: IE's energy gauge, the two hopper slots, the jam message and the Clear Belt
 * button, on a background made by art/gui/AssemblerGui.java.
 */
public class AmmoAssemblerScreen extends IEContainerScreen<AmmoAssemblerMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ImmersiveMunitions.MODID, "textures/gui/ammo_assembler.png");
    private static final String LANG = "gui.immersive_munitions.ammo_assembler";
    // Layout, matching the background image
    private static final int TITLE_Y = 9, JAM_Y = 20, SLOTS_CENTRE_X = 82;
    private static final int BUTTON_X = 50, BUTTON_Y = 53, BUTTON_W = 64, BUTTON_H = 16;
    private static final int GAUGE_X = 157, GAUGE_Y = 13;
    private static final int JAM_COLOUR = 0xffff5555;

    private Button clearBelt;

    public AmmoAssemblerScreen(AmmoAssemblerMenu menu, Inventory player, Component title) {
        super(menu, player, title, TEXTURE);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelY = TITLE_Y;
        clearBelt = addRenderableWidget(Button.builder(Component.translatable(LANG + ".clear_belt"), button -> {
                    CompoundTag message = new CompoundTag();
                    message.putBoolean(AmmoAssemblerMenu.CLEAR_BELT, true);
                    sendUpdateToServer(message);
                })
                .bounds(leftPos + BUTTON_X, topPos + BUTTON_Y, BUTTON_W, BUTTON_H)
                .build());
    }

    @Nonnull
    @Override
    protected List<InfoArea> makeInfoAreas() {
        return ImmutableList.of(new EnergyInfoArea(leftPos + GAUGE_X, topPos + GAUGE_Y, menu.energy));
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        ItemStack need = menu.waitingFor();
        if (need.isEmpty()) return;
        Component line = Component.translatable(LANG + ".needs", need.getHoverName());
        graphics.drawString(font, line, SLOTS_CENTRE_X - font.width(line) / 2, JAM_Y, JAM_COLOUR, true);
    }

    @Override
    protected void gatherAdditionalTooltips(int mouseX, int mouseY, Consumer<Component> addLine, Consumer<Component> addGray) {
        if (clearBelt != null && clearBelt.isHovered()) {
            addLine.accept(Component.translatable(LANG + ".clear_belt"));
            for (int i = 1; i <= 3; i++) addGray.accept(Component.translatable(LANG + ".clear_belt.tooltip_" + i));
            return;
        }
        // an empty slot says what goes in it
        Slot slot = hoveredSlot;
        if (slot == null || slot.hasItem() || !menu.getCarried().isEmpty()) return;
        if (slot.index == AmmoAssemblerMenu.GUNPOWDER_SLOT) addLine.accept(Component.translatable(LANG + ".gunpowder"));
        else if (slot.index == AmmoAssemblerMenu.PROJECTILE_SLOT) addLine.accept(Component.translatable(LANG + ".projectiles"));
    }

    @Mod.EventBusSubscriber(modid = ImmersiveMunitions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> MenuScreens.register(ModMenus.AMMO_ASSEMBLER.get(), AmmoAssemblerScreen::new));
        }

        private Registration() {}
    }
}
