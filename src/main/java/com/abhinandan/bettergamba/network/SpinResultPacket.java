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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Server to Client: notifies the client of the spin outcome tier.
 * Used to display the rarity color and tier name in the GUI.
 *
 * @param tierName  e.g. "Omega"
 * @param tierColor ARGB int — matches the 5 tier colors from Phase 1
 */
public record SpinResultPacket(BlockPos pos, String tierName, int tierColor) implements CustomPacketPayload {

    public static final Type<SpinResultPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BetterGamba.MOD_ID, "spin_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpinResultPacket> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, SpinResultPacket::pos, ByteBufCodecs.STRING_UTF8, SpinResultPacket::tierName, ByteBufCodecs.INT, SpinResultPacket::tierColor, SpinResultPacket::new);

    public static void handle(SpinResultPacket packet, @NotNull IPayloadContext context) {
        context.enqueueWork(() -> {
            var screen = Minecraft.getInstance().screen;
            if (screen instanceof LotteryMachineScreen lms && lms.getMenu().getBlockPos().equals(packet.pos())) {
                lms.onSpinResult(packet.tierName(), packet.tierColor());
            }
        });
    }

    @Override
    public @NotNull Type<SpinResultPacket> type() {
        return TYPE;
    }

    @Contract(pure = true)
    public static int colourForTier(@NotNull String tierName) {
        return switch (tierName) {
            case "Common" -> 0xFF607D8B;
            case "Uncommon" -> 0xFF2E7D32;
            case "Rare" -> 0xFF1565C0;
            case "Epic" -> 0xFF6A1B9A;
            case "Omega" -> 0xFFB71C1C;
            default -> 0xFFFFFFFF;
        };
    }
}