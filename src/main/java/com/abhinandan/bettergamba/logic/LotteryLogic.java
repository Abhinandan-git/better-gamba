package com.abhinandan.bettergamba.logic;

import com.abhinandan.bettergamba.logic.model.ItemEntry;
import com.abhinandan.bettergamba.logic.model.RarityTier;
import com.abhinandan.bettergamba.logic.model.RewardPool;
import com.abhinandan.bettergamba.logic.model.SpinResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

/**
 * Pure spin resolution engine for Better Gamba.
 *
 * <p>This class has ZERO Minecraft imports and ZERO side effects.
 * It accepts a RewardPool and a Random source, and returns a SpinResult.
 * All Minecraft-specific work (ItemStack, world drops) is done by the caller.
 *
 * <p>Algorithm (RWD-04):
 * 1. Sum weights of all selectable tiers (weight > 0 AND items not empty).
 * 2. If total weight is 0, return SpinResult.failure().
 * 3. Draw a random integer R in [0, totalWeight).
 * 4. Iterate selectable tiers, accumulating weights.
 * The first tier where accumulated weight > R wins.
 * 5. Select one item uniformly at random from the winning tier's list.
 */
public final class LotteryLogic {
    private LotteryLogic() {
    }

    /**
     * Resolves a single spin against the given reward pool.
     *
     * @param pool   The reward pool to spin against
     * @param random Random source — use new Random() in production,
     *               new Random(seed) in tests for determinism
     * @return SpinResult with outcome, or SpinResult.failure() if
     * no tiers are selectable
     */
    public static @NotNull SpinResult spin(@NotNull RewardPool pool, Random random) {
        int totalWeight = pool.totalWeight();
        if (totalWeight <= 0) {
            return SpinResult.failure();
        }

        // Weighted tier selection
        int roll = random.nextInt(totalWeight);
        int accumulated = 0;

        for (RarityTier tier : pool.tiers()) {
            if (!tier.isSelectable()) continue;
            accumulated += tier.weight();
            if (roll < accumulated) {
                // This tier wins — pick one item uniformly at random
                List<ItemEntry> items = tier.items();
                ItemEntry chosen = items.get(random.nextInt(items.size()));
                return new SpinResult(tier.name(), chosen, true);
            }
        }

        // Unreachable if totalWeight > 0, but defensive fallback
        return SpinResult.failure();
    }

    /**
     * Returns true if the given itemId is the registered celestia_coin.
     * Used by the coin slot validator to reject non-coin items (BLK-04).
     *
     * @param itemRegistryId The full registry ID of the item to check
     * @param modId          The mod ID, used to build the expected coin ID
     */
    public static boolean isCelestiaCoin(String itemRegistryId, String modId) {
        return (modId + ":celestia_coin").equals(itemRegistryId);
    }
}
