package com.abhinandan.bettergamba.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Defines the TOML configuration schema for Better Gamba.
 *
 * <p>Generated file location: config/bettergamba-common.toml
 * The PO edits this file to configure coin cost, spin duration,
 * rarity weights, and per-tier item lists.
 *
 * <p>Schema design (RWD-01, RWD-02, NFR-02):
 * Each rarity tier has:
 * weight  — integer, controls relative probability
 * items   — list of strings, format: "namespace:path" or
 * "namespace:path|{nbt}" for optional NBT
 */
public class BetterGambaConfig {
    public static final BetterGambaConfig INSTANCE;
    public static final ModConfigSpec SPEC;

    static {
        Pair<BetterGambaConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(BetterGambaConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    /**
     * Number of celestia_coins consumed per spin. Default: 1.
     */
    public final ModConfigSpec.IntValue coinCostPerSpin;

    /**
     * Spin timer duration in milliseconds. Default: 1500 (1.5 seconds).
     */
    public final ModConfigSpec.IntValue spinDurationMs;

    /**
     * Whether spin events are logged at INFO level. Default: true.
     */
    public final ModConfigSpec.BooleanValue logSpinEvents;

    // -- Rarity: Common --------------------------------------------
    public final ModConfigSpec.IntValue commonWeight;
    public final ModConfigSpec.ConfigValue<List<? extends String>> commonItems;

    // -- Rarity: Uncommon ------------------------------------------
    public final ModConfigSpec.IntValue uncommonWeight;
    public final ModConfigSpec.ConfigValue<List<? extends String>> uncommonItems;

    // -- Rarity: Rare ----------------------------------------------
    public final ModConfigSpec.IntValue rareWeight;
    public final ModConfigSpec.ConfigValue<List<? extends String>> rareItems;

    // -- Rarity: Epic ----------------------------------------------
    public final ModConfigSpec.IntValue epicWeight;
    public final ModConfigSpec.ConfigValue<List<? extends String>> epicItems;

    // -- Rarity: Omega ---------------------------------------------
    public final ModConfigSpec.IntValue omegaWeight;
    public final ModConfigSpec.ConfigValue<List<? extends String>> omegaItems;

    private BetterGambaConfig(ModConfigSpec.@NotNull Builder builder) {
        builder.comment("Better Gamba - Global Configuration").push("general");
        coinCostPerSpin = builder.comment("Number of Celestia Coins consumed per spin. Minimum: 1.").defineInRange("coinCostPerSpin", 1, 1, 64);
        spinDurationMs = builder.comment("Spin animation duration in milliseconds (1000-3000).").defineInRange("spinDurationMs", 1500, 1000, 3000);
        logSpinEvents = builder.comment("Log spin outcomes (player, position, tier, item) at INFO level.").define("logSpinEvents", true);
        builder.pop();

        builder.comment("Reward Pool — Common tier").push("common");
        commonWeight = builder.comment("Relative weight. Higher = more frequent. Must blockEntity >= 0.").defineInRange("weight", 100, 0, Integer.MAX_VALUE);
        commonItems = builder.comment("Item list. Format: \"namespace:path\" or \"namespace:path|{nbt}\"").defineListAllowEmpty("items", List.of("minecraft:bread", "minecraft:apple"), BetterGambaConfig::isValidItemEntry);
        builder.pop();

        builder.comment("Reward Pool — Uncommon tier").push("uncommon");
        uncommonWeight = builder.comment("Relative weight.").defineInRange("weight", 60, 0, Integer.MAX_VALUE);
        uncommonItems = builder.comment("Item list.").defineListAllowEmpty("items", List.of("minecraft:gold_ingot"), BetterGambaConfig::isValidItemEntry);
        builder.pop();

        builder.comment("Reward Pool — Rare tier").push("rare");
        rareWeight = builder.comment("Relative weight.").defineInRange("weight", 30, 0, Integer.MAX_VALUE);
        rareItems = builder.comment("Item list.").defineListAllowEmpty("items", List.of("minecraft:diamond"), BetterGambaConfig::isValidItemEntry);
        builder.pop();

        builder.comment("Reward Pool — Epic tier").push("epic");
        epicWeight = builder.comment("Relative weight.").defineInRange("weight", 10, 0, Integer.MAX_VALUE);
        epicItems = builder.comment("Item list.").defineListAllowEmpty("items", List.of("minecraft:emerald"), BetterGambaConfig::isValidItemEntry);
        builder.pop();

        builder.comment("Reward Pool — Omega tier").push("omega");
        omegaWeight = builder.comment("Relative weight.").defineInRange("weight", 1, 0, Integer.MAX_VALUE);
        omegaItems = builder.comment("Item list.").defineListAllowEmpty("items", List.of("minecraft:nether_star"), BetterGambaConfig::isValidItemEntry);
        builder.pop();
    }

    /**
     * Validates a single item entry string from the TOML.
     * Valid formats:
     * "minecraft:diamond"              — plain item ID
     * "minecraft:diamond|{Count:2b}"   — item ID with NBT suffix
     * <p>
     * Only the format is validated here. Whether the item ID actually
     * exists in the registry is checked separately by ConfigValidator
     * at server start (after all mods have registered their items).
     */
    public static boolean isValidItemEntry(Object obj) {
        if (!(obj instanceof String s)) return false;
        if (s.isBlank()) return false;
        String id = s.contains("|") ? s.substring(0, s.indexOf('|')) : s;
        // Must match namespace:path pattern
        return id.matches("[a-z0-9_.-]+:[a-z0-9_./+-]+");
    }
}
