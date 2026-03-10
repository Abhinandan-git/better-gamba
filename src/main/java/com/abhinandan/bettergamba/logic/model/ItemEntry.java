package com.abhinandan.bettergamba.logic.model;

import java.util.Optional;

/**
 * A single item entry in a rarity tier's item pool.
 *
 * <p>Parsed from a TOML string of the form:
 * "namespace:path"            — plain item, no NBT
 * "namespace:path|{nbt}"      — item with optional NBT tag string
 *
 * <p>This is a pure Java record — no Minecraft imports.
 * The BlockEntity converts this to an ItemStack when delivering a reward.
 *
 * @param registryId The full registry ID, e.g. "minecraft:diamond"
 * @param nbtString  Optional raw NBT string, e.g. "{Count:2b}"
 */
public record ItemEntry(String registryId, Optional<String> nbtString) {
    /**
     * Parses a raw TOML entry string into an ItemEntry.
     *
     * @param raw The raw string from TOML, e.g. "minecraft:diamond" or
     *            "minecraft:diamond|{Count:2b}"
     * @return Parsed ItemEntry
     * @throws IllegalArgumentException if the format is invalid
     */
    public static ItemEntry parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("ItemEntry raw string must not blockEntity blank");
        }

        int pipeIndex = raw.indexOf('|');
        if (pipeIndex == -1) {
            return new ItemEntry(raw.trim(), Optional.empty());
        }

        String id = raw.substring(0, pipeIndex).trim();
        String nbt = raw.substring(pipeIndex + 1).trim();
        return new ItemEntry(id, nbt.isEmpty() ? Optional.empty() : Optional.of(nbt));
    }
}
