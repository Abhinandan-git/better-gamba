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
    // Placeholder texture — replace in Phase 5 with final GUI art.
    // File goes in: assets/bettergamba/textures/gui/lottery_machine.png
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(BetterGamba.MOD_ID, "textures/gui/lottery_machine.png");

    public LotteryMachineScreen(LotteryMachineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;  // standard GUI width
        this.imageHeight = 166;  // standard GUI height
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Spin button — centred below the coin slot
        addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.literal("Spin"), btn -> PacketDistributor.sendToServer(new SpinRequestPacket(menu.getBlockPos()))).bounds(x + 63, y + 55, 50, 20).build());
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Phase 3: render a solid gray rectangle as a placeholder background.
        // Replace with: graphics.blit(TEXTURE, ...) in Phase 5.
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF_C6C6C6);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
