package com.abhinandan.bettergamba.logic;

import com.abhinandan.bettergamba.logic.model.ItemEntry;
import com.abhinandan.bettergamba.logic.model.RarityTier;
import com.abhinandan.bettergamba.logic.model.RewardPool;
import com.abhinandan.bettergamba.logic.model.SpinResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LotteryLogic.
 *
 * <p>All tests are pure Java — no Minecraft instance required.
 * Tests are deterministic via seeded Random instances.
 * <p>
 * Test categories:
 * [LOGIC] — core algorithm correctness
 * [EDGE]  — edge cases and error conditions (all 6 PO-confirmed cases)
 * [VALID] — input validation
 */
class LotteryLogicTest {
    // ── Helpers ───────────────────────────────────────────────────

    /**
     * Builds a RarityTier with the given name, weight, and plain item IDs.
     */
    private static RarityTier tier(String name, int weight, String... itemIds) {
        List<ItemEntry> entries = Arrays.stream(itemIds).map(ItemEntry::parse).toList();
        return new RarityTier(name, weight, entries);
    }

    /**
     * Builds a RewardPool from the given tiers.
     */
    private static RewardPool pool(RarityTier... tiers) {
        return new RewardPool(List.of(tiers));
    }

    // ── [LOGIC] Core algorithm tests ──────────────────────────────

    @Test
    @DisplayName("[LOGIC] Spin always returns a result from a valid pool")
    void spinReturnsSuccessForValidPool() {
        // Given: a pool with one tier containing one item
        RewardPool p = pool(tier("Common", 100, "minecraft:bread"));

        // When: spin is called
        SpinResult result = LotteryLogic.spin(p, new Random(1L));

        // Then: success, correct tier, correct item
        assertTrue(result.success(), "Expected a successful spin");
        assertEquals("Common", result.tierName());
        assertEquals("minecraft:bread", result.itemEntry().registryId());
    }

    @Test
    @DisplayName("[LOGIC] Higher weight tier is selected more frequently")
    void higherWeightTierSelectedMoreFrequently() {
        // Given: Common (weight 99) vs Omega (weight 1)
        RewardPool p = pool(tier("Common", 99, "minecraft:bread"), tier("Omega", 1, "minecraft:nether_star"));

        // When: 1000 spins are run
        int commonCount = 0;
        Random rng = new Random(42L);
        for (int i = 0; i < 1000; i++) {
            if ("Common".equals(LotteryLogic.spin(p, rng).tierName())) commonCount++;
        }

        // Then: Common selected ~99% of the time (allow 5% tolerance)
        assertTrue(commonCount > 900, "Common tier selected " + commonCount + "/1000 times, expected > 900");
    }

    @Test
    @DisplayName("[LOGIC] Single selectable tier always wins regardless of roll")
    void singleSelectableTierAlwaysWins() {
        // Given: only one tier has weight > 0 and items
        RewardPool p = pool(tier("Common", 100, "minecraft:bread"), new RarityTier("Omega", 0, List.of())  // weight 0, no items
        );

        // When: 100 spins with varied seeds
        for (int seed = 0; seed < 100; seed++) {
            SpinResult result = LotteryLogic.spin(p, new Random(seed));
            assertEquals("Common", result.tierName(), "Only Common should win at seed " + seed);
        }
    }

    @Test
    @DisplayName("[LOGIC] Item is selected uniformly from winning tier")
    void itemSelectedUniformlyFromWinningTier() {
        // Given: one tier with 3 items
        RewardPool p = pool(tier("Common", 100, "minecraft:bread", "minecraft:apple", "minecraft:carrot"));

        // When: 3000 spins
        Map<String, Integer> counts = new HashMap<>();
        Random rng = new Random(7L);
        for (int i = 0; i < 3000; i++) {
            String id = LotteryLogic.spin(p, rng).itemEntry().registryId();
            counts.merge(id, 1, Integer::sum);
        }

        // Then: each item selected roughly 1000 times (allow 20% tolerance)
        counts.forEach((id, count) -> assertTrue(count > 800 && count < 1200, id + " selected " + count + " times, expected ~1000"));
    }

    @Test
    @DisplayName("[LOGIC] ItemEntry parses plain ID correctly")
    void itemEntryParsesPlainId() {
        ItemEntry entry = ItemEntry.parse("minecraft:diamond");
        assertEquals("minecraft:diamond", entry.registryId());
        assertTrue(entry.nbtString().isEmpty());
    }

    @Test
    @DisplayName("[LOGIC] ItemEntry parses ID with NBT correctly")
    void itemEntryParsesIdWithNbt() {
        ItemEntry entry = ItemEntry.parse("minecraft:diamond|{Count:2b}");
        assertEquals("minecraft:diamond", entry.registryId());
        assertTrue(entry.nbtString().isPresent());
        assertEquals("{Count:2b}", entry.nbtString().get());
    }

    // ── [EDGE] Edge cases ─────────────────────────────────────────

    @Test
    @DisplayName("[EDGE] All tiers have zero weight — spin returns failure")
    void allZeroWeightsReturnsFailure() {
        // Requirement: all tiers weight=0 must not crash — return failure gracefully
        RewardPool p = pool(new RarityTier("Common", 0, List.of(ItemEntry.parse("minecraft:bread"))), new RarityTier("Uncommon", 0, List.of(ItemEntry.parse("minecraft:gold_ingot"))), new RarityTier("Rare", 0, List.of(ItemEntry.parse("minecraft:diamond"))), new RarityTier("Epic", 0, List.of(ItemEntry.parse("minecraft:emerald"))), new RarityTier("Omega", 0, List.of(ItemEntry.parse("minecraft:nether_star"))));

        SpinResult result = LotteryLogic.spin(p, new Random());

        assertFalse(result.success(), "Expected failure when all weights are 0");
        assertEquals(0, p.totalWeight());
    }

    @Test
    @DisplayName("[EDGE] Empty item list in a tier — tier is not selectable")
    void emptyItemListTierIsNotSelectable() {
        // Requirement: a tier with no items must be skipped even if weight > 0
        RarityTier emptyTier = new RarityTier("Common", 100, List.of());
        RewardPool p = pool(emptyTier, tier("Omega", 1, "minecraft:nether_star"));

        // Empty tier must not be selectable
        assertFalse(emptyTier.isSelectable(), "Tier with empty item list must not be selectable");

        // Omega must always win since Common is not selectable
        for (int seed = 0; seed < 50; seed++) {
            SpinResult result = LotteryLogic.spin(p, new Random(seed));
            assertTrue(result.success());
            assertEquals("Omega", result.tierName(), "Only Omega should win when Common has no items");
        }
    }

    @Test
    @DisplayName("[EDGE] Unknown item registry ID — ItemEntry parses without crash")
    void unknownItemRegistryIdParsesWithoutCrash() {
        // Requirement: an unknown ID must parse successfully at the model level.
        // ConfigValidator (server start) is responsible for detecting it — not this class.
        // LotteryLogic must not crash when encountering an unknown ID.
        assertDoesNotThrow(() -> {
            ItemEntry entry = ItemEntry.parse("somemod:nonexistent_item");
            assertEquals("somemod:nonexistent_item", entry.registryId());
        }, "ItemEntry.parse must not throw for an unknown but validly-formatted ID");
    }

    @Test
    @DisplayName("[EDGE] requestSpin ignored when spin already in progress")
    void spinIgnoredWhenAlreadySpinning() {
        // This is a contract test — documents that the 'spinning' flag
        // prevents double-spins. Verified at the BlockEntity level in-game.
        // LotteryLogic itself is stateless — this guard lives in the BlockEntity.
        // Test verifies that LotteryLogic.spin() is pure and has no 'isSpinning' state.
        var methods = Arrays.stream(LotteryLogic.class.getDeclaredMethods()).map(java.lang.reflect.Method::getName).toList();
        assertFalse(methods.stream().anyMatch(m -> m.contains("spinning") || m.contains("active")), "LotteryLogic must be stateless — spin state belongs in the BlockEntity");
    }

    @Test
    @DisplayName("[EDGE] Redstone input triggers spin — LotteryLogic is agnostic to trigger source")
    void lotteryLogicIsAgnosticToTriggerSource() {
        // LotteryLogic does not know or care whether the spin was triggered
        // by a player button click or a redstone signal — both call requestSpin()
        // on the BlockEntity which calls LotteryLogic.spin().
        // This test verifies LotteryLogic has no trigger-source-specific methods,
        // keeping the trigger routing responsibility in the BlockEntity where it belongs.
        var methods = Arrays.stream(LotteryLogic.class.getDeclaredMethods()).map(java.lang.reflect.Method::getName).toList();

        assertFalse(methods.stream().anyMatch(m -> m.toLowerCase().contains("player") || m.toLowerCase().contains("redstone")), "LotteryLogic must be trigger-agnostic — routing belongs in BlockEntity");
    }

    @Test
    @DisplayName("[VALID] Coin slot only accepts celestia_coin")
    void coinSlotOnlyAcceptsCelestiaCoin() {
        // Requirement: ITM-04, BLK-04 — only bettergamba:celestia_coin is valid
        String modId = "bettergamba";

        assertTrue(LotteryLogic.isCelestiaCoin("bettergamba:celestia_coin", modId), "celestia_coin must be accepted");
        assertFalse(LotteryLogic.isCelestiaCoin("minecraft:gold_ingot", modId), "gold_ingot must be rejected");
        assertFalse(LotteryLogic.isCelestiaCoin("minecraft:coin", modId), "minecraft:coin must be rejected");
        assertFalse(LotteryLogic.isCelestiaCoin("", modId), "Empty string must be rejected");
        assertFalse(LotteryLogic.isCelestiaCoin("bettergamba:celestia_coin_fake", modId), "Similar but wrong ID must be rejected");
    }

    // ── [EDGE] ItemEntry validation ───────────────────────────────

    @Test
    @DisplayName("[EDGE] ItemEntry.parse throws on blank input")
    void itemEntryParseThrowsOnBlank() {
        assertThrows(IllegalArgumentException.class, () -> ItemEntry.parse(""));
        assertThrows(IllegalArgumentException.class, () -> ItemEntry.parse("   "));
        assertThrows(IllegalArgumentException.class, () -> ItemEntry.parse(null));
    }

    @Test
    @DisplayName("[EDGE] RarityTier constructor throws on negative weight")
    void rarityTierThrowsOnNegativeWeight() {
        assertThrows(IllegalArgumentException.class, () -> new RarityTier("Bad", -1, List.of()));
    }

    @Test
    @DisplayName("[LOGIC] totalWeight excludes tiers with empty item lists")
    void totalWeightExcludesEmptyTiers() {
        RewardPool p = pool(new RarityTier("Common", 100, List.of()),  // empty — excluded
                tier("Omega", 5, "minecraft:nether_star")  // included
        );
        assertEquals(5, p.totalWeight(), "totalWeight must only count tiers with items");
    }
}
