package com.abhinandan.bettergamba.item;

import net.minecraft.world.item.Item;

/**
 * The Lottery Coin — the token currency consumed by the Lottery Machine.
 *
 * <p>This item intentionally has no crafting recipe defined in the mod.
 * Acquisition is delegated entirely to the modpack via KubeJS scripts.
 * Stack size is vanilla default (64) per ITM-05.
 */
public class CelestiaCoinItem extends Item {
    public CelestiaCoinItem() {
        super(new Item.Properties());
    }
}
