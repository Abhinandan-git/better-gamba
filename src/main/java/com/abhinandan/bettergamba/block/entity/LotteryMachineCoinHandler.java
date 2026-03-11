package com.abhinandan.bettergamba.block.entity;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Side-aware IItemHandler for the Lottery Machine.
 *
 * <p>Exposes the coin slot based on which side a hopper is connected to:
 * <ul>
 *   <li>LEFT / RIGHT (relative to block FACING): input only — hoppers may insert coins.
 *   <li>BOTTOM: output only — hoppers may extract reward items.
 *   <li>All other sides: returns an empty handler (no access).
 * </ul>
 *
 * <p>Left/right are resolved by the caller (RegisterCapabilitiesEvent lambda)
 * which receives the absolute Direction and the block's FACING value.
 */
public class LotteryMachineCoinHandler implements IItemHandler {
    private final LotteryMachineBlockEntity blockEntity;
    private final boolean allowInsert;
    private final boolean allowExtract;

    public LotteryMachineCoinHandler(LotteryMachineBlockEntity lotteryMachineBlockEntity, boolean allowInsert, boolean allowExtract) {
        this.blockEntity = lotteryMachineBlockEntity;
        this.allowInsert = allowInsert;
        this.allowExtract = allowExtract;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return blockEntity.coinInventory.getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack itemStack, boolean simulate) {
        if (!allowInsert) {
            return itemStack;
        }
        return blockEntity.coinInventory.insertItem(slot, itemStack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!allowExtract) {
            return ItemStack.EMPTY;
        }
        return blockEntity.coinInventory.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return blockEntity.coinInventory.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack itemStack) {
        if (!allowInsert) {
            return false;
        }
        return blockEntity.coinInventory.isItemValid(slot, itemStack);
    }
}
