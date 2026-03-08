package com.abhinandan.bettergamba.block;

import com.abhinandan.bettergamba.block.entity.LotteryMachineBlockEntity;
import com.abhinandan.bettergamba.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Lottery Machine block.
 *
 * <p>Visually 2 blocks tall but occupies a single block in the world (ADR-01).
 * The VoxelShape is extended upward to cover the visual upper half, preventing
 * normal block placement in that space (ADR-01, VoxelShape approach).
 *
 * <p>Facing direction is stored in BlockState via FACING property.
 * Hopper side logic reads this property to resolve left/right directions (BLK-05).
 */
public class LotteryMachineBlock extends BaseEntityBlock {
    /**
     * FACING stores which direction the block's 'front' faces when placed.
     * Used by hopper capability to resolve left/right sides relative to the player.
     */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    /**
     * CODEC serialize and deserialize the block state.
     */
    public static final MapCodec<LotteryMachineBlock> CODEC = simpleCodec(LotteryMachineBlock::new);

    /**
     * The collision/visual shape of the block.
     * Extends from y=0 to y=2 (2 full block heights) within the 16x16 XZ footprint.
     * This prevents players from placing blocks in the visual upper half (ADR-01).
     * <p>
     * Note: VoxelShape coordinates are in 1/16th-block units.
     * A full block is 0,0,0 to 16,16,16. Two blocks tall is 0,0,0 to 16,32,16.
     */
    private static final VoxelShape SHAPE = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 2.0D, 1.0D);

    public LotteryMachineBlock(Properties properties) {
        super(properties);
        // Register default block state: facing NORTH
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    // -- Shape (ADR-01) ------------------------------------------------
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos position, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos position, @NotNull CollisionContext context) {
        return SHAPE;
    }

    // -- BlockState ----------------------------------------------------
    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /**
     * Sets the FACING direction when the block is placed.
     * The block faces toward the player who placed it — matching vanilla machine convention.
     */
    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    // -- Interaction ---------------------------------------------------

    /**
     * Opens the Lottery Machine GUI when a player right-clicks the block.
     * Server-side only — client sends a useItem packet, server opens the menu.
     */
    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos position, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(position);

            if (blockEntity instanceof LotteryMachineBlockEntity lotteryMachineBlockEntity) {
                player.openMenu(lotteryMachineBlockEntity, buf -> buf.writeBlockPos(position));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    // -- BlockEntity ---------------------------------------------------
    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        // INVISIBLE defers rendering to a BlockEntityRenderer.
        // Change to MODEL once a static JSON model is used (Phase 5).
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new LotteryMachineBlockEntity(blockPos, blockState);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, @NotNull BlockPos position, @NotNull BlockState state, @NotNull Player player) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(position);
            if (blockEntity instanceof LotteryMachineBlockEntity lotteryMachineBlockEntity) {
                lotteryMachineBlockEntity.dropContents();
            }
        }
        super.playerWillDestroy(level, position, state, player);
        return state;
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /**
     * Registers the server-side tick method for the BlockEntity.
     * Client-side ticking is not needed — no world-space animation in v1.
     */
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.LOTTERY_MACHINE_BLOCK_ENTITY.get(), LotteryMachineBlockEntity::tick);
    }

    /**
     * Declares that this block can receive redstone signals.
     * Required for canConnectRedstone and neighbourChanged to fire correctly.
     */
    @Override
    public boolean canConnectRedstone(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    /**
     * Called when a neighboring block changes — checks for redstone signal.
     * If the block receives power and is not already spinning, triggers a spin.
     * This allows automation without a player present (new requirement).
     */
    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block changedBlock, @NotNull BlockPos changedPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, changedBlock, changedPos, isMoving);
        if (level.isClientSide()) return;

        boolean isPowered = level.hasNeighborSignal(pos);
        if (!isPowered) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LotteryMachineBlockEntity lmbe) {
            lmbe.requestSpin();
        }
    }
}
