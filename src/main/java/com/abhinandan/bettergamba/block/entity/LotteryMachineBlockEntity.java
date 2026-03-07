package com.abhinandan.bettergamba.block.entity;

import com.abhinandan.bettergamba.registry.ModBlockEntities;
import com.abhinandan.bettergamba.screen.menu.LotteryMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * BlockEntity for the Lottery Machine.
 *
 * <p>Holds a single-slot coin inventory (BLK-04, BLK-05).
 * NBT save/load ensures coins persist across chunk unloads and server restarts.
 * Thread-safe spin queue will be added in Phase 4 (ADR-03).
 */
public class LotteryMachineBlockEntity extends BlockEntity implements MenuProvider {
    /**
     * The coin slot inventory.
     * SLOT_COUNT = 1: a single slot holds the coins awaiting a spin.
     * onContentsChanged triggers setChanged() so the chunk is marked dirty for saving.
     */
    public static final int SLOT_COUNT = 1;
    public static final int COIN_SLOT = 0;
    /**
     * NBT key for the coin inventory. Must not be changed after any world save data.
     */
    private static final String NBT_INVENTORY = "inventory";
    public final ItemStackHandler coinInventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public LotteryMachineBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.LOTTERY_MACHINE_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(NBT_INVENTORY, coinInventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(NBT_INVENTORY)) {
            coinInventory.deserializeNBT(registries, tag.getCompound(NBT_INVENTORY));
        }
    }

    /**
     * Drops all coins held in the inventory at the block's position.
     * Called by LotteryMachineBlock when the block is destroyed.
     * Uses Containers.dropContents — identical pattern to Chest and Furnace.
     */
    public void dropContents() {
        if (level == null) {
            return;
        }

        SimpleContainer inventory = new SimpleContainer(SLOT_COUNT);
        inventory.setItem(COIN_SLOT, coinInventory.getStackInSlot(COIN_SLOT));
        Containers.dropContents(level, worldPosition, inventory);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.bettergamba.lottery_machine");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new LotteryMachineMenu(containerId, inventory, this);
    }
}
