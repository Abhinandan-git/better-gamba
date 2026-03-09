package com.abhinandan.bettergamba.integration.jei;

import com.abhinandan.bettergamba.BetterGamba;
import com.abhinandan.bettergamba.config.BetterGambaConfig;
import com.abhinandan.bettergamba.logic.model.ItemEntry;
import com.abhinandan.bettergamba.logic.model.RarityTier;
import com.abhinandan.bettergamba.logic.model.RewardPool;
import com.abhinandan.bettergamba.registry.ModBlocks;
import com.abhinandan.bettergamba.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class BetterGambaJeiPlugin implements IModPlugin {
    private static ItemStack entryToStack(@NotNull ItemEntry entry) {
        var loc = ResourceLocation.tryParse(entry.registryId());
        if (loc == null || !BuiltInRegistries.ITEM.containsKey(loc)) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(BuiltInRegistries.ITEM.get(loc));
    }

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(BetterGamba.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new LotteryMachineCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        RewardPool pool = RewardPool.fromConfig(BetterGambaConfig.INSTANCE);
        ItemStack coin = new ItemStack(ModItems.CELESTIA_COIN.get());

        List<LotteryMachineJeiRecipe> recipes = new ArrayList<>();
        for (RarityTier tier : pool.tiers()) {
            if (!tier.isSelectable()) continue;
            List<ItemStack> outputs = tier.items().stream().map(BetterGambaJeiPlugin::entryToStack).filter(s -> !s.isEmpty()).toList();
            if (!outputs.isEmpty()) {
                recipes.add(new LotteryMachineJeiRecipe(tier.name(), coin.copy(), outputs));
            }
        }
        registration.addRecipes(LotteryMachineCategory.TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.LOTTERY_MACHINE.get()), LotteryMachineCategory.TYPE);
    }
}
