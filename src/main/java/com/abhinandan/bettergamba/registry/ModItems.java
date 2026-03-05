package com.abhinandan.bettergamba.registry;

import com.abhinandan.bettergamba.BetterGamba;
import com.abhinandan.bettergamba.item.CelestiaCoinItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BetterGamba.MOD_ID);

    /**
     * The lottery coin. Consumed by the Lottery Machine per spin.
     * Registry ID: bettergamba:celestia_coin
     */
    public static final DeferredHolder<Item, CelestiaCoinItem> CELESTIA_COIN = ITEMS.register("celestia_coin", CelestiaCoinItem::new);

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
