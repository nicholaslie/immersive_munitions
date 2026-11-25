package dev.immersive_munitions.client.jei;

import dev.immersive_munitions.ImmersiveMunitions;
import dev.immersive_munitions.crafting.AmmoAssemblyRecipe;
import dev.immersive_munitions.registry.ModMultiblocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

/** JEI page for an Ammunition Assembler recipe: casing, gunpowder and projectile in, the TaCZ round out. */
public class AmmoAssemblyCategory implements IRecipeCategory<AmmoAssemblyRecipe> {
    public static final RecipeType<AmmoAssemblyRecipe> TYPE =
            RecipeType.create(ImmersiveMunitions.MODID, "ammo_assembly", AmmoAssemblyRecipe.class);

    // Layout: three input slots, the arrow, the output slot, a text line under them
    private static final int WIDTH = 124, HEIGHT = 42;
    private static final int SLOT_Y = 6, ARROW_X = 62, ARROW_Y = 6, OUTPUT_X = 98, TEXT_Y = 30;

    private final IDrawable icon;
    private final IDrawableStatic slot, outputSlot, arrow;

    public AmmoAssemblyCategory(IGuiHelper gui) {
        icon = gui.createDrawableItemStack(ModMultiblocks.AMMO_ASSEMBLER.iconStack());
        slot = gui.getSlotDrawable();
        outputSlot = gui.getOutputSlot();
        arrow = gui.getRecipeArrow();
    }

    @Override
    public RecipeType<AmmoAssemblyRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.immersive_munitions.ammo_assembler");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AmmoAssemblyRecipe recipe, IFocusGroup focuses) {
        input(builder, 1, recipe.casing.getMatchingStacks());
        input(builder, 21, recipe.gunpowder.getMatchingStacks());
        input(builder, 41, recipe.projectile.getMatchingStacks());
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + 5, SLOT_Y)
                .setBackground(outputSlot, -5, -5)
                .addItemStacks(recipe.getItemOutputs());
    }

    private void input(IRecipeLayoutBuilder builder, int x, ItemStack[] stacks) {
        builder.addSlot(RecipeIngredientRole.INPUT, x, SLOT_Y)
                .setBackground(slot, -1, -1)
                .addItemStacks(List.copyOf(Arrays.asList(stacks)));
    }

    @Override
    public void draw(AmmoAssemblyRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        arrow.draw(graphics, ARROW_X, ARROW_Y);
        Font font = Minecraft.getInstance().font;
        Component line = Component.translatable("jei.immersive_munitions.ammo_assembly.cost",
                recipe.getTotalProcessEnergy(), recipe.getTotalProcessTime() / 20f);
        graphics.drawString(font, line, (WIDTH - font.width(line)) / 2, TEXT_Y, 0xff808080, false);
    }
}
