package com.abhinandan.bettergamba.network;

import com.abhinandan.bettergamba.BetterGamba;
import com.abhinandan.bettergamba.block.entity.LotteryMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Client → Server packet: player requests a spin at the given block position.
 *
 * <p>Sent when the player clicks the Spin button in the GUI.
 * Server validates coin count and queues the spin request.
 */
public record SpinRequestPacket(BlockPos blockPos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SpinRequestPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(BetterGamba.MOD_ID, "spin_request"));

    public static final StreamCodec<FriendlyByteBuf, SpinRequestPacket> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, SpinRequestPacket::blockPos, SpinRequestPacket::new);

    /**
     * Handles the packet on the server thread.
     * Looks up the BlockEntity and calls requestSpin().
     */
    public static void handle(SpinRequestPacket packet, @NotNull IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;
            var blockEntity = sp.level().getBlockEntity(packet.blockPos());
            if (blockEntity instanceof LotteryMachineBlockEntity lotteryMachineBlockEntity) {
                lotteryMachineBlockEntity.requestSpin();
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
