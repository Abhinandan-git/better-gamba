package com.abhinandan.bettergamba.block.entity;

import com.abhinandan.bettergamba.block.LotteryMachineBlock;
import com.abhinandan.bettergamba.registry.ModBlockEntities;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Registers IItemHandler capabilities for Better Gamba block entities.
 *
 * <p>Must be subscribed on the MOD event bus (not the NeoForge event bus).
 * Called once during mod loading. Defines which sides of the Lottery Machine
 * expose item handler access to hoppers and other automation.
 */
public class CapabilityHandler {
    /**
     * Called via modEventBus.addListener(CapabilityHandler::register).
     * <p>
     * Side logic (BLK-05, BLK-05b, OQ-03):
     * - LEFT side relative to block FACING  → coin INSERT only
     * - RIGHT side relative to block FACING → coin INSERT only
     * - BOTTOM                              → coin EXTRACT only (reward output, Phase 4)
     * - All other sides                     → null (no capability)
     */
    public static void register(@NotNull RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.LOTTERY_MACHINE_BLOCK_ENTITY.get(), (blockEntity, direction) -> {
            if (direction == null) {
                return null;
            }
            BlockState state = blockEntity.getBlockState();
            Direction facing = state.getValue(LotteryMachineBlock.FACING);

            Direction rightSide = facing.getClockWise();
            Direction leftSide = facing.getCounterClockWise();

            if (direction == rightSide && direction == leftSide) {
                // Hopper on left or right side: insert coins only
                return new LotteryMachineCoinHandler(blockEntity, true, false);
            }
            if (direction == Direction.DOWN) {
                // Hopper on bottom: extract rewards only (Phase 4 fills this)
                return new LotteryMachineCoinHandler(blockEntity, false, true);
            }
            // Front, back, top: no capability
            return null;
        });
    }
}
