package com.abhinandan.bettergamba.screen;

import com.abhinandan.bettergamba.BetterGamba;
import com.abhinandan.bettergamba.network.SpinRequestPacket;
import com.abhinandan.bettergamba.screen.menu.LotteryMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

/**
 * Client-side screen for the Lottery Machine.
 *
 * <p>Phase 3: renders a placeholder gray background with the coin slot.
 * Phase 5 will replace this with the final GUI texture, rarity display,
 * and the spin animation timer.
 */
public class LotteryMachineScreen extends AbstractContainerScreen<LotteryMachineMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(BetterGamba.MOD_ID, "textures/gui/lottery_machine.png");
    private String lastTierName = "";
    private int lastTierColour = 0xFFFFFFFF;
    private int tierDisplayTicks = 0;

    public LotteryMachineScreen(LotteryMachineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;  // standard GUI width
        this.imageHeight = 166;  // standard GUI height
    }

    public void onSpinResult(String tierName, int colour) {
        this.lastTierName = tierName;
        this.lastTierColour = colour;
        this.tierDisplayTicks = 60;
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Spin button — centred below the coin slot
        addRenderableWidget(Button.builder(Component.literal("Spin"), btn -> PacketDistributor.sendToServer(new SpinRequestPacket(menu.getBlockPos()))).bounds(x + 115, y + 119, 34, 18).build());
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Phase 3: render a solid gray rectangle as a placeholder background.
        // Replace with: graphics.blit(TEXTURE, ...) in Phase 5.
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        if (tierDisplayTicks > 0) {
            tierDisplayTicks--;
            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;
            // Draw tier name centered
            graphics.drawCenteredString(font, lastTierName, x + imageWidth / 2, y + 80, lastTierColour);
        }
    }
}
