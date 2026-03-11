package com.abhinandan.bettergamba.network;

import com.abhinandan.bettergamba.BetterGamba;
import com.abhinandan.bettergamba.screen.LotteryMachineScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Server → Client: fired when a spin starts.
 * Carries spinDurationMs so the client animation length
 * matches the server timer exactly.
 */
public record SpinStartPacket(BlockPos pos, int spinDurationMs) implements CustomPacketPayload {

    public static final Type<SpinStartPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BetterGamba.MOD_ID, "spin_start"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpinStartPacket> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, SpinStartPacket::pos, ByteBufCodecs.INT, SpinStartPacket::spinDurationMs, SpinStartPacket::new);

    public static void handle(SpinStartPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var screen = Minecraft.getInstance().screen;
            if (screen instanceof LotteryMachineScreen lms && lms.getMenu().getBlockPos().equals(packet.pos())) {
                lms.onSpinStart(packet.spinDurationMs());
            }
        });
    }

    @Override
    public @NotNull Type<SpinStartPacket> type() {
        return TYPE;
    }
}