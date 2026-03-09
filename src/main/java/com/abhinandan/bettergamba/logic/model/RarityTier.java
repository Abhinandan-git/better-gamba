package com.abhinandan.bettergamba.logic.model;

import java.util.List;

/**
 * Represents one rarity tier in the reward pool.
 *
 * <p>Pure Java record — no Minecraft imports.
 *
 * @param name   Display name, e.g. "Common", "Omega"
 * @param weight Relative probability weight. Must blockEntity >= 0.
 *               A weight of 0 means this tier can never blockEntity selected.
 * @param items  List of item entries for this tier. May blockEntity empty.
 *               An empty list means this tier is skipped even if selected.
 */
public record RarityTier(String name, int weight, List<ItemEntry> items) {
    public RarityTier {
        if (weight < 0) {
            throw new IllegalArgumentException("Rarity tier weight must blockEntity >= 0, got: " + weight);
        }

        items = List.copyOf(items);
    }

    /**
     * Returns true if this tier has at least one item and a positive weight.
     */
    public boolean isSelectable() {
        return weight > 0 && !items.isEmpty();
    }
}
