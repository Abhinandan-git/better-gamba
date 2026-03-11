package com.abhinandan.bettergamba.config;

import com.abhinandan.bettergamba.BetterGamba;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Validates TOML item registry IDs at server start and on /reload.
 *
 * <p>Logs a WARNING for each unresolved ID rather than crashing —
 * allowing the server to start with a degraded config rather than
 * not at all.
 */
@EventBusSubscriber(modid = BetterGamba.MOD_ID)
public class ConfigValidator {

    private static final Logger LOGGER = LogManager.getLogger(BetterGamba.MOD_ID);

    /**
     * Called at server start via @SubscribeEvent.
     * Also called on /reload via BetterGamba.onDatapackSync().
     */
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        validate(BetterGambaConfig.INSTANCE);
    }

    /**
     * Validates all reward tiers in the given config.
     * Safe to call at any time — used by both server start and /reload.
     *
     * @param cfg the live config instance to validate
     */
    public static void validate(@NotNull BetterGambaConfig cfg) {
        LOGGER.info("[BetterGamba] Validating reward pool configuration...");

        validateTier("common", cfg.commonItems.get());
        validateTier("uncommon", cfg.uncommonItems.get());
        validateTier("rare", cfg.rareItems.get());
        validateTier("epic", cfg.epicItems.get());
        validateTier("omega", cfg.omegaItems.get());

        LOGGER.info("[BetterGamba] Reward pool validation complete.");
    }

    private static void validateTier(String tierName, @NotNull List<? extends String> items) {
        if (items.isEmpty()) {
            LOGGER.warn("[BetterGamba] Tier '{}' has no items configured. " + "This tier will never be selected during spin resolution.", tierName);
            return;
        }
        for (String entry : items) {
            String id = entry.contains("|") ? entry.substring(0, entry.indexOf('|')) : entry;
            ResourceLocation loc = ResourceLocation.tryParse(id);
            if (loc == null || !BuiltInRegistries.ITEM.containsKey(loc)) {
                LOGGER.warn("[BetterGamba] Tier '{}': unknown item '{}'. " + "It will be skipped during spin resolution.", tierName, id);
            }
        }
    }
}