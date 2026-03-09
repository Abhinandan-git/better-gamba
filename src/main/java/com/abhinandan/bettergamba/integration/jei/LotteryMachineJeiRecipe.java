package com.abhinandan.bettergamba.integration.jei;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * JEI recipe data carrier for one rarity tier.
 * One LotteryMachineJeiRecipe per tier = 5 total entries in JEI.
 *
 * @param tierName      Display name shown in JEI entry title
 * @param coinInput     The celestia_coin ItemStack
 * @param rewardOutputs All possible reward items in this tier
 */
public record LotteryMachineJeiRecipe(String tierName, ItemStack coinInput, List<ItemStack> rewardOutputs) {
}
