package com.abhinandan.bettergamba;

import com.abhinandan.bettergamba.block.entity.CapabilityHandler;
import com.abhinandan.bettergamba.config.BetterGambaConfig;
import com.abhinandan.bettergamba.registry.ModBlockEntities;
import com.abhinandan.bettergamba.registry.ModBlocks;
import com.abhinandan.bettergamba.registry.ModItems;
import com.abhinandan.bettergamba.registry.ModMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.jetbrains.annotations.NotNull;

/**
 * Better Gamba — main mod entry point.
 * Registers event buses and initializes all DeferredRegisters.
 *
 * @author Abhinandan
 * @version 1.0.0
 */
@Mod(BetterGamba.MOD_ID)
public class BetterGamba {
    public static final String MOD_ID = "bettergamba";

    public BetterGamba(IEventBus modEventBus, @NotNull ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, BetterGambaConfig.SPEC);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        modEventBus.addListener(CapabilityHandler::register);
    }
}
