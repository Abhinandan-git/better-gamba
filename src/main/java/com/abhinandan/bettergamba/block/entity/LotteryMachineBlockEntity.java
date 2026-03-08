package com.abhinandan.bettergamba.block.entity;

import com.abhinandan.bettergamba.BetterGamba;
import com.abhinandan.bettergamba.config.BetterGambaConfig;
import com.abhinandan.bettergamba.logic.LotteryLogic;
import com.abhinandan.bettergamba.logic.model.ItemEntry;
import com.abhinandan.bettergamba.logic.model.RewardPool;
import com.abhinandan.bettergamba.logic.model.SpinResult;
import com.abhinandan.bettergamba.registry.ModBlockEntities;
import com.abhinandan.bettergamba.screen.menu.LotteryMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Random;

/**
 * BlockEntity for the Lottery Machine.
 *
 * <p>Holds a single-slot coin inventory (BLK-04, BLK-05).
 * NBT save/load ensures coins persist across chunk unloads and server restarts.
 * Thread-safe spin queue will blockEntity added in Phase 4 (ADR-03).
 */
public class LotteryMachineBlockEntity extends BlockEntity implements MenuProvider {
    /**
     * The coin slot inventory.
     * SLOT_COUNT = 1: a single slot holds the coins awaiting a spin.
     * onContentsChanged triggers setChanged() so the chunk is marked dirty for saving.
     */
    public static final int SLOT_COUNT = 1;
    public static final int COIN_SLOT = 0;
    public static final int REWARD_SLOT = 0;
    /**
     * NBT key for the coin inventory. Must not blockEntity changed after any world save data.
     */
    private static final String NBT_INVENTORY = "inventory";
    private static final String NBT_REWARD = "reward";
    private static final Logger LOGGER = LogManager.getLogger(BetterGamba.MOD_ID);
    public final ItemStackHandler coinInventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        /**
         * Restricts the coin slot to bettergamba:celestia_coin only.
         * Rejects all other items — they cannot blockEntity inserted via GUI or hopper.
         * Requirement: ITM-04, BLK-04.
         */
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            String registryId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            return LotteryLogic.isCelestiaCoin(registryId, BetterGamba.MOD_ID);
        }
    };
    public final ItemStackHandler rewardInventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final Random random = new Random();
    /**
     * True while a spin is currently resolving. Shared across all players.
     */
    private boolean spinning = false;
    /**
     * Ticks remaining in the current spin animation. 0 = no active spin.
     */
    private int spinTicksRemaining = 0;

    public LotteryMachineBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.LOTTERY_MACHINE_BLOCK_ENTITY.get(), pos, blockState);
    }

    public static void tick(@NotNull Level level, BlockPos pos, BlockState state, LotteryMachineBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        drainRewardOutput(level, pos, blockEntity);

        if (blockEntity.spinTicksRemaining > 0) {
            blockEntity.spinTicksRemaining--;
            if (blockEntity.spinTicksRemaining == 0) {
                blockEntity.spinning = false;
            }
            return;
        }

        if (!blockEntity.spinning) {
            return;
        }

        RewardPool pool = RewardPool.fromConfig(BetterGambaConfig.INSTANCE);
        SpinResult result = LotteryLogic.spin(pool, blockEntity.random);

        if (!result.success()) {
            LOGGER.warn("[BetterGamba] Spin failed — reward pool misconfigured.");
            blockEntity.spinning = false;
            return;
        }

        blockEntity.deliverReward(result, pos, level);

        if (BetterGambaConfig.INSTANCE.logSpinEvents.get()) {
            LOGGER.info("[BetterGamba] Spin at {} — Tier: {}, Item: {}", pos, result.tierName(), result.itemEntry().registryId());
        }

        int spinMs = BetterGambaConfig.INSTANCE.spinDurationMs.get();
        blockEntity.spinTicksRemaining = Math.max(1, spinMs / 50);
        level.updateNeighborsAt(pos, state.getBlock());
    }

    private static void drainRewardOutput(Level level, BlockPos pos, @NotNull LotteryMachineBlockEntity blockEntity) {
        ItemStack reward = blockEntity.rewardInventory.getStackInSlot(REWARD_SLOT);
        if (reward.isEmpty()) {
            return;
        }

        // Try bottom hopper first (BLK-05b)
        var belowHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.below(), null);
        if (belowHandler == null) {
            belowHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.below(), Direction.UP);
        }

        LOGGER.info("[BetterGamba] drainRewardOutput: belowHandler={}, rewardStack={}", belowHandler, reward);

        if (belowHandler != null) {
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(belowHandler, reward.copy(), false);
            // Extract exactly how much was consumed
            int consumed = reward.getCount() - remainder.getCount();
            if (consumed > 0) {
                blockEntity.rewardInventory.extractItem(REWARD_SLOT, consumed, false);
                reward = blockEntity.rewardInventory.getStackInSlot(REWARD_SLOT);
            }
            if (reward.isEmpty()) {
                return;
            }
        }

        // No hopper or hopper full — drop remainder in world
        blockEntity.rewardInventory.extractItem(REWARD_SLOT, reward.getCount(), false);
        ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, reward);
        level.addFreshEntity(itemEntity);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(NBT_INVENTORY, coinInventory.serializeNBT(registries));
        tag.put(NBT_REWARD, rewardInventory.serializeNBT(registries));
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.bettergamba.lottery_machine");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new LotteryMachineMenu(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(NBT_INVENTORY)) {
            coinInventory.deserializeNBT(registries, tag.getCompound(NBT_INVENTORY));
        }
        if (tag.contains(NBT_REWARD)) {
            rewardInventory.deserializeNBT(registries, tag.getCompound(NBT_REWARD));
        }
    }

    /**
     * Called when any player clicks the Spin button.
     * If a spin is already in progress, the request is ignored — the button
     * should blockEntity visually disabled on the client during this time (Phase 5).
     */
    public void requestSpin() {
        if (spinning) return;

        int coinCost = BetterGambaConfig.INSTANCE.coinCostPerSpin.get();
        ItemStack coinsInSlot = coinInventory.getStackInSlot(COIN_SLOT);

        if (coinsInSlot.isEmpty() || coinsInSlot.getCount() < coinCost) return;

        coinInventory.extractItem(COIN_SLOT, coinCost, false);
        spinning = true;
        setChanged();
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

        SimpleContainer inventory = new SimpleContainer(2);
        inventory.setItem(0, coinInventory.getStackInSlot(COIN_SLOT));
        inventory.setItem(1, rewardInventory.getStackInSlot(REWARD_SLOT));
        Containers.dropContents(level, worldPosition, inventory);
    }

    /**
     * Constructs an ItemStack from an ItemEntry.
     * Resolves the registry ID. If the item does not exist, returns ItemStack.EMPTY
     * and logs a warning (ConfigValidator should have caught this at server start).
     */
    private ItemStack buildItemStack(@UnknownNullability ItemEntry entry) {
        ResourceLocation loc = ResourceLocation.tryParse(entry.registryId());
        if (loc == null || !BuiltInRegistries.ITEM.containsKey(loc)) {
            LOGGER.warn("[BetterGamba] Cannot deliver reward: unknown item '{}'", entry.registryId());
            return ItemStack.EMPTY;
        }
        var item = BuiltInRegistries.ITEM.get(loc);
        var stack = new ItemStack(item);
        // NBT application will be added in Phase 5 when NBT parsing is implemented
        return stack;
    }

    /**
     * Delivers the reward item — either into a bottom hopper or as a world drop.
     * (OQ-10, BLK-05b)
     *
     * @param result The resolved spin result
     * @param pos    The block's world position
     * @param level  The server level
     */
    private void deliverReward(@NotNull SpinResult result, BlockPos pos, Level level) {
        ItemStack reward = buildItemStack(result.itemEntry());
        if (reward.isEmpty()) {
            return;
        }

        // Try inserting into rewardInventory slot first
        ItemStack remainder = rewardInventory.insertItem(REWARD_SLOT, reward, false);

        // If rewardInventory is full, drop directly in the world as fallback
        if (!remainder.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, remainder);
            level.addFreshEntity(itemEntity);
        }
    }
}
