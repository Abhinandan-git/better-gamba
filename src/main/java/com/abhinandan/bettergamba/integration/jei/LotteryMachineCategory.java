package com.abhinandan.bettergamba.integration.jei;

import com.abhinandan.bettergamba.BetterGamba;
import com.abhinandan.bettergamba.registry.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * JEI category for the Lottery Machine reward pool.
 * Displays: coin input on the left, all possible rewards on the right.
 */
public class LotteryMachineCategory implements IRecipeCategory<LotteryMachineJeiRecipe> {

    public static final RecipeType<LotteryMachineJeiRecipe> TYPE = new RecipeType<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(BetterGamba.MOD_ID, "lottery_machine"), LotteryMachineJeiRecipe.class);

    private static final int WIDTH = 160;
    private static final int HEIGHT = 40;

    private final IDrawable icon;

    public LotteryMachineCategory(@NotNull IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.LOTTERY_MACHINE.get()));
    }

    @Override
    public @NotNull RecipeType<LotteryMachineJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.bettergamba.lottery_machine");
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
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull LotteryMachineJeiRecipe recipe, @NotNull IFocusGroup focuses) {
        // Coin input slot on the left
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 12).addItemStack(recipe.coinInput());

        // Reward output slots in a row
        List<ItemStack> outputs = recipe.rewardOutputs();
        for (int idx = 0; idx < outputs.size(); idx++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 30 + idx * 20, 12).addItemStack(outputs.get(idx));
        }
    }

    @Override
    public void draw(@NotNull LotteryMachineJeiRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Optional: draw tier name as a label above the slots
        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, recipe.tierName(), 1, 1, 0xFF607D8B,
                false);
    }
}