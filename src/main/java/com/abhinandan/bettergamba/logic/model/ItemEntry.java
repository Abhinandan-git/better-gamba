package com.abhinandan.bettergamba.logic.model;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A single item entry in a rarity tier's item pool.
 *
 * <p>Format: "namespace:path" or "namespace:path|quantity" or
 * "namespace:path{nbt}|quantity"
 *
 * @param registryId The full registry ID, e.g. "minecraft:diamond"
 * @param quantity   Stack size 1–64. Defaults to 1.
 * @param nbtString  Optional raw NBT string
 */
public record ItemEntry(String registryId, int quantity, Optional<String> nbtString) {
    @Contract("null -> fail")
    public static @NotNull ItemEntry parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("ItemEntry raw string must not be blank");
        }

        String id = raw.trim();
        int qty = 1;
        Optional<String> nbt = Optional.empty();

        // Extract NBT — everything from first { to last }
        if (id.contains("{")) {
            int nbtStart = id.indexOf('{');
            int nbtEnd = id.lastIndexOf('}');
            if (nbtStart >= 0 && nbtEnd > nbtStart) {
                nbt = Optional.of(id.substring(nbtStart, nbtEnd + 1));
                id = id.substring(0, nbtStart) + id.substring(nbtEnd + 1);
            }
        }

        // Extract quantity after pipe
        if (id.contains("|")) {
            String[] parts = id.split("\\|", 2);
            id = parts[0].trim();
            try {
                qty = Integer.parseInt(parts[1].trim());
                qty = Math.max(1, Math.min(qty, 64));
            } catch (NumberFormatException e) {
                qty = 1;
            }
        } else {
            id = id.trim();
        }

        return new ItemEntry(id, qty, nbt);
    }
}