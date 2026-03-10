package com.abhinandan.bettergamba.logic.model;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * The outcome of a single spin.
 *
 * <p>Pure Java record — returned by LotteryLogic.spin().
 * The BlockEntity converts this to an ItemStack for delivery.
 *
 * @param tierName  The name of the winning rarity tier, e.g. "Omega"
 * @param itemEntry The selected item entry from that tier
 * @param success   False if spin could not blockEntity resolved (all tiers misconfigured)
 */
public record SpinResult(String tierName, ItemEntry itemEntry, boolean success) {
    /**
     * Factory method for a failed spin — no tiers were selectable.
     */
    @Contract(" -> new")
    public static @NotNull SpinResult failure() {
        return new SpinResult("", null, false);
    }
}
