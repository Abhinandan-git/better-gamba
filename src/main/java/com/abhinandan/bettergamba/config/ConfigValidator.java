package com.abhinandan.bettergamba.config;

import com.abhinandan.bettergamba.BetterGamba;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Logger;

/**
 * Validates TOML item registry IDs at server start.
 *
 * <p>Runs after all mods have registered their items so every valid
 * modpack item ID is resolvable. Logs a WARNING for each unresolved ID
 * rather than crashing — allowing the server to start with a degraded
 * config rather than not at all.
 */
@EventBusSubscriber(modid = BetterGamba.MOD_ID)
public class ConfigValidator {
    private static final Logger LOGGER = Logger.getLogger(BetterGamba.MOD_ID);

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("[BetterGamba] Validating reward pool configuration...");
        BetterGambaConfig cfg = BetterGambaConfig.INSTANCE;

        validateTier("common", cfg.commonItems.get());
        validateTier("uncommon", cfg.uncommonItems.get());
        validateTier("rare", cfg.rareItems.get());
        validateTier("epic", cfg.epicItems.get());
        validateTier("omega", cfg.omegaItems.get());

        LOGGER.info("[BetterGamba] Reward pool validation complete.");
    }

    private static void validateTier(String tierName, @NotNull List<? extends String> items) {
        if (items.isEmpty()) {
            LOGGER.warning("[BetterGamba] Tier '{}' has no items configured. " + tierName + "This tier will never be selected during spin resolution.");
            return;
        }
        for (String entry : items) {
            String id = entry.contains("|") ? entry.substring(0, entry.indexOf('|')) : entry;
            ResourceLocation loc = ResourceLocation.tryParse(id);
            if (loc == null || !BuiltInRegistries.ITEM.containsKey(loc)) {
                LOGGER.warning("[BetterGamba] Tier '{}': unknown item '{}'. " + tierName + id + "It will be skipped during spin resolution.");
            }
        }
    }
}
