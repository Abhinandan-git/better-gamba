package com.abhinandan.bettergamba.screen.menu;

import com.abhinandan.bettergamba.block.entity.LotteryMachineBlockEntity;
import com.abhinandan.bettergamba.registry.ModBlocks;
import com.abhinandan.bettergamba.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class LotteryMachineMenu extends AbstractContainerMenu {
    public static final int COIN_SLOT_INDEX = 0;
    private final LotteryMachineBlockEntity blockEntity;

    /**
     * Server-side constructor — called when a player opens the GUI.
     */
    public LotteryMachineMenu(int containerId, Inventory playerInventory, @NotNull LotteryMachineBlockEntity blockEntity) {
        super(ModMenuTypes.LOTTERY_MACHINE_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        // Position (x=80, y=35): centred above player inventory in the GUI.
        addSlots(playerInventory);
    }

    /**
     * Client-side constructor — called when the client receives the open-menu packet.
     * Reads block position from the extra data buffer to find the BlockEntity.
     */
    public LotteryMachineMenu(int containerId, @NotNull Inventory playerInventory, @NotNull FriendlyByteBuf extraData) {
        super(ModMenuTypes.LOTTERY_MACHINE_MENU.get(), containerId);
        BlockPos position = extraData.readBlockPos();
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(position);
        if (blockEntity instanceof LotteryMachineBlockEntity lotteryMachineBlockEntity) {
            this.blockEntity = lotteryMachineBlockEntity;
        } else {
            throw new IllegalStateException("No LotteryMachineBlockEntity at " + position);
        }
        addSlots(playerInventory);
    }

    /**
     * Shared slot registration — called by both constructors.
     * Extracted to avoid duplication since this() chaining is not permitted
     * when the delegated constructor itself calls a static method.
     */
    private void addSlots(Inventory playerInventory) {
        addSlot(new SlotItemHandler(blockEntity.coinInventory, COIN_SLOT_INDEX, 138, 201));

        // Player hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 48 + col * 18, 223));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        // Shift-click handling — move items between player inventory and coin slot.
        ItemStack returnStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            returnStack = slotStack.copy();
            if (index == COIN_SLOT_INDEX) {
                // From coin slot -> player inventory
                if (!moveItemStackTo(slotStack, 1, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player inventory -> coin slot
                if (!moveItemStackTo(slotStack, COIN_SLOT_INDEX, COIN_SLOT_INDEX + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (slotStack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
            if (slotStack.getCount() == returnStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, slotStack);
        }
        return returnStack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        assert blockEntity.getLevel() != null;
        return AbstractContainerMenu.stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, ModBlocks.LOTTERY_MACHINE.get());
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }
}