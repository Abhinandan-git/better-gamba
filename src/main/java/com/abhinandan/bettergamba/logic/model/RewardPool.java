package com.abhinandan.bettergamba.logic.model;

import com.abhinandan.bettergamba.config.BetterGambaConfig;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The complete reward pool — all 5 rarity tiers loaded from config.
 *
 * <p>Built once at server start from BetterGambaConfig and cached.
 * Rebuilt if the config is reloaded (NeoForge /reload command).
 */
public record RewardPool(List<RarityTier> tiers) {
    public RewardPool {
        tiers = List.copyOf(tiers);
    }

    /**
     * Builds a RewardPool from the live BetterGambaConfig.
     * Called by the BlockEntity on server start and after /reload.
     */
    @Contract("_ -> new")
    public static @NotNull RewardPool fromConfig(@NotNull BetterGambaConfig cfg) {
        return new RewardPool(List.of(buildTier("Common", cfg.commonWeight.get(), cfg.commonItems.get()), buildTier("Uncommon", cfg.uncommonWeight.get(), cfg.uncommonItems.get()), buildTier("Rare", cfg.rareWeight.get(), cfg.rareItems.get()), buildTier("Epic", cfg.epicWeight.get(), cfg.epicItems.get()), buildTier("Omega", cfg.omegaWeight.get(), cfg.omegaItems.get())));
    }

    private static @NotNull RarityTier buildTier(String name, int weight, @NotNull List<? extends String> rawItems) {
        List<ItemEntry> entries = rawItems.stream().map(ItemEntry::parse).toList();
        return new RarityTier(name, weight, entries);
    }

    /**
     * Total weight of all selectable tiers. Returns 0 if no tiers are selectable.
     */
    public int totalWeight() {
        return tiers.stream().filter(RarityTier::isSelectable).mapToInt(RarityTier::weight).sum();
    }
}
