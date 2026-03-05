package com.abhinandan.bettergamba.registry;

import com.abhinandan.bettergamba.BetterGamba;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BetterGamba.MOD_ID);

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
