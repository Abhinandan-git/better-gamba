package com.abhinandan.bettergamba.registry;

import com.abhinandan.bettergamba.BetterGamba;
import com.abhinandan.bettergamba.block.entity.LotteryMachineBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BetterGamba.MOD_ID);

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LotteryMachineBlockEntity>> LOTTERY_MACHINE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("lottery_machine", () -> BlockEntityType.Builder.of(LotteryMachineBlockEntity::new, ModBlocks.LOTTERY_MACHINE.get()).build(null));


}
