package com.abhinandan.bettergamba;

import com.abhinandan.bettergamba.registry.ModBlockEntities;
import com.abhinandan.bettergamba.registry.ModBlocks;
import com.abhinandan.bettergamba.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

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

    public BetterGamba(IEventBus modEventBus) {
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
    }
}
