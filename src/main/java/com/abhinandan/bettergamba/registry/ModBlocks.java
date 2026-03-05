package com.abhinandan.bettergamba.registry;

import com.abhinandan.bettergamba.BetterGamba;
import com.abhinandan.bettergamba.block.LotteryMachineBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BetterGamba.MOD_ID);

    /**
     * The Lottery Machine block.
     * Registry ID: bettergamba:lottery_machine
     * Requires a corresponding BlockItem so players can hold and place it.
     */
    public static final DeferredBlock<LotteryMachineBlock> LOTTERY_MACHINE = BLOCKS.register("lottery_machine", () -> new LotteryMachineBlock(
            BlockBehaviour.Properties.of().strength(3.5f, 6.0f).sound(SoundType.WOOD).noOcclusion()
    ));

    /**
     * BlockItem for the Lottery Machine — allows the block to exist in inventory.
     * KubeJS defines the crafting recipe; this just registers the item form.
     */
    public static final DeferredHolder<Item, BlockItem> LOTTERY_MACHINE_ITEM = ModItems.ITEMS.register("lottery_machine", () -> new BlockItem(LOTTERY_MACHINE.get(), new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
