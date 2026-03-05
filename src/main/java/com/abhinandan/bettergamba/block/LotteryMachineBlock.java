package com.abhinandan.bettergamba.block;

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
    public static VoxelShape getSHAPE(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
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

    @Override
    protected @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    // -- Interaction ---------------------------------------------------

    /**
     * Opens the Lottery Machine GUI when a player right-clicks the block.
     * Server-side only — client sends a useItem packet, server opens the menu.
     */
    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            // if (blockEntity instanceof LotteryMachineBlockEntity lotteryMachineBlockEntity) {
            //     // TODO [Phase 3 Step 26]: open menu here once LotteryMachineMenu exists
            //     // player.openMenu(lotteryMachineBlockEntity, pos);
            // }
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

    // @Override
    // public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
    //     return new LotteryMachineBlockEntity(blockPos, blockState);
    // }
    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return null;
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
