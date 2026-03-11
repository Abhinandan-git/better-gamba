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

import java.util.Random;

public class LotteryMachineScreen extends AbstractContainerScreen<LotteryMachineMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(BetterGamba.MOD_ID, "textures/gui/lottery_machine.png");
    private static final ResourceLocation WHEEL = ResourceLocation.fromNamespaceAndPath(BetterGamba.MOD_ID, "textures/gui/lottery_wheel.png");

    // ── Single control point for wheel position ───────────────────────────────
    /**
     * Move the entire wheel (texture + highlight) left/right. Positive = right.
     */
    private static final int WHEEL_OFFSET_X = 0;
    /**
     * Move the entire wheel (texture + highlight) up/down. Positive = down.
     */
    private static final int WHEEL_OFFSET_Y = -34;
    /**
     * Wheel radius in pixels.
     */
    private static final int WHEEL_RADIUS = 97;
    /**
     * Radius of the dead zone at the wheel center — no highlight drawn here.
     */
    private static final int WHEEL_INNER_RADIUS = 4;
    /**
     * Display size of the wheel on screen — independent of texture file size.
     */
    private static final int WHEEL_DISPLAY_SIZE = 245;
    /**
     * Texture file dimensions.
     */
    private static final int WHEEL_TEXTURE_SIZE = 500;

    private static final int SECTIONS = 5;
    private static final float TWO_PI = (float) (Math.PI * 2);
    private static final float SWEEP = TWO_PI / SECTIONS;
    private static final float HIGHLIGHT_OFFSET_DEG = 53f;
    // ── Section → tier mapping (index 0‥4, anti-clockwise from bottom) ───────────────
    private static final String[] TIER_NAMES = {"Common", "Uncommon", "Rare", "Epic", "Omega"};
    /**
     * Ticks between each highlight jump. Starts at 2 (fast), grows to 14 (slow).
     */
    private static final int INTERVAL_START = 10;

    // ── Animation state ───────────────────────────────────────────────────────
    private static final int INTERVAL_MAX = 25;
    private static final float START_ANGLE = (float) (Math.PI / 2);
    private static final long FADE_DURATION_MS = 3000L;
    private final Random random = new Random();
    /**
     * True while the post-spin cooldown is active — button disabled.
     */
    private boolean onCooldown = false;
    /**
     * Currently highlighted section index (0‥4). -1 = none.
     */
    private int highlightIndex = -1;
    private int highlightTickCount = 0;
    private int highlightIntervalTicks = INTERVAL_START;
    private boolean spinning = false;
    private int stepsRemaining = 0;

    // ── Fade-out state ────────────────────────────────────────────────────────
    private int winnerIndex = -1;
    private boolean hasResult = false;
    /**
     * White overlay alpha (0‥255). -1 = no active fade.
     */
    private int highlightAlpha = -1;
    private long fadeStartMs = -1;
    private Button spinButton;
    private int lastTierColour = 0xFFFFFFFF;
    // ── Result display ────────────────────────────────────────────────────────
    private String lastTierName = "";

    public LotteryMachineScreen(LotteryMachineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 256;
    }

    // ── Packet callbacks ──────────────────────────────────────────────────────

    public void onSpinStart(int durationMs) {
        // Clear any previous fade
        highlightAlpha = -1;
        fadeStartMs = -1;
        lastTierName = "";

        spinning = true;
        hasResult = false;
        winnerIndex = -1;
        highlightTickCount = 0;
        highlightIntervalTicks = INTERVAL_START;
        highlightIndex = random.nextInt(SECTIONS);

        int spinTicks = Math.max(60, durationMs / 50);
        stepsRemaining = spinTicks / highlightIntervalTicks;
    }

    public void onSpinResult(@NotNull String tierName, int colour) {
        // Empty tierName = spin was canceled — reset all state
        if (tierName.isEmpty()) {
            spinning = false;
            highlightIndex = -1;
            highlightAlpha = -1;
            fadeStartMs = -1;
            lastTierName = "";
            stepsRemaining = 0;
            hasResult = false;
            onCooldown = false;
            return;
        }
        this.lastTierName = tierName;
        this.lastTierColour = colour;
        this.hasResult = true;
        this.winnerIndex = indexOfTier(tierName);
        this.onCooldown = true;
    }

    private int indexOfTier(String name) {
        for (int i = 0; i < TIER_NAMES.length; i++) {
            if (TIER_NAMES[i].equals(name)) return i;
        }
        return 0;
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        spinButton = Button.builder(Component.literal("Spin"), btn -> PacketDistributor.sendToServer(new SpinRequestPacket(menu.getBlockPos()))).bounds(x + 101, y + 200, 34, 18).build();
        addRenderableWidget(spinButton);
    }

    // ── Animation tick ────────────────────────────────────────────────────────

    private void tickAnimation() {
        // Fade-out runs independently of spin state
        if (highlightAlpha >= 0) {
            long now = System.currentTimeMillis();
            long elapsed = now - fadeStartMs;

            if (elapsed >= FADE_DURATION_MS) {
                highlightAlpha = -1;
                fadeStartMs = -1;
                highlightIndex = -1;
                lastTierName = "";
                onCooldown = false;
            } else {
                highlightAlpha = (int) (255 * (1.0f - elapsed / (float) FADE_DURATION_MS));
            }
        }

        if (!spinning) return;

        highlightTickCount++;
        if (highlightTickCount < highlightIntervalTicks) return;
        highlightTickCount = 0;

        // Gradually slow down throughout the entire spin — 1 tick added every step
        if (highlightIntervalTicks < INTERVAL_MAX) {
            highlightIntervalTicks++;
        }

        // If result is known and steps are exhausted — land on winner and stop
        if (hasResult && stepsRemaining <= 0) {
            highlightIndex = winnerIndex;
            spinning = false;
            highlightAlpha = 63;
            fadeStartMs = System.currentTimeMillis();
            return;
        }

        // Result not yet known and steps ran out — keep cycling until result arrives
        if (stepsRemaining <= 0) {
            int next;
            do {
                next = random.nextInt(SECTIONS);
            } while (next == highlightIndex);
            highlightIndex = next;
            return;
        }

        stepsRemaining--;

        // Random jump — never stay on same section
        int next;
        do {
            next = random.nextInt(SECTIONS);
        } while (next == highlightIndex);
        highlightIndex = next;
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Draw GUI background
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // Single origin for both wheel texture and highlight
        int wheelX = (width - WHEEL_DISPLAY_SIZE) / 2 + WHEEL_OFFSET_X;
        int wheelY = (height - WHEEL_DISPLAY_SIZE) / 2 + WHEEL_OFFSET_Y;

        // Draw wheel texture
        graphics.blit(WHEEL, wheelX, wheelY, WHEEL_DISPLAY_SIZE, WHEEL_DISPLAY_SIZE, 0, 0, WHEEL_TEXTURE_SIZE, WHEEL_TEXTURE_SIZE, WHEEL_TEXTURE_SIZE, WHEEL_TEXTURE_SIZE);

        tickAnimation();

        // Highlight uses exact same origin — always in sync
        if (highlightIndex >= 0) {
            int cx = wheelX + WHEEL_DISPLAY_SIZE / 2;
            int cy = wheelY + WHEEL_DISPLAY_SIZE / 2;
            drawFullCircleOverlay(graphics, cx, cy, highlightIndex);
            graphics.flush();
        }
    }

    /**
     * Draws the full circle overlay:
     * - Highlighted section: white at given alpha
     * - All other sections: black at 60% opacity (153/255)
     */
    private void drawFullCircleOverlay(GuiGraphics graphics, int cx, int cy, int index) {
        int highlightAlphaValue = spinning ? 127 : Math.max(0, highlightAlpha);
        if (highlightAlphaValue == 0 && index < 0) return;

        int dimColour = (76 << 24); // 30% Opacity
        int highlightColour = (highlightAlphaValue << 24) | 0x00FFFFFF; // white

        for (int px = cx - WHEEL_RADIUS; px <= cx + WHEEL_RADIUS; px++) {
            int dx = px - cx;
            int halfH = (int) Math.sqrt(WHEEL_RADIUS * WHEEL_RADIUS - dx * dx);
            if (halfH == 0) continue;

            // Find strip bounds per section
            int[] sectionTop = new int[SECTIONS];
            int[] sectionBottom = new int[SECTIONS];
            for (int s = 0; s < SECTIONS; s++) {
                sectionTop[s] = Integer.MAX_VALUE;
                sectionBottom[s] = Integer.MIN_VALUE;
            }

            for (int py = cy - halfH; py <= cy + halfH; py++) {
                int dy = py - cy;

                if (dx * dx + dy * dy < WHEEL_INNER_RADIUS * WHEEL_INNER_RADIUS) continue;

                float angle = (float) Math.atan2(dy, dx);

                // Find which section this pixel belongs to
                float offsetRad = (float) Math.toRadians(HIGHLIGHT_OFFSET_DEG);
                int section = -1;
                for (int s = 0; s < SECTIONS; s++) {
                    if (isAngleInWedge(angle, -(s * SWEEP) + START_ANGLE + offsetRad, -(s * SWEEP) + START_ANGLE + offsetRad - SWEEP)) {
                        section = s;
                        break;
                    }
                }
                if (section < 0) continue;

                if (py < sectionTop[section]) sectionTop[section] = py;
                if (py > sectionBottom[section]) sectionBottom[section] = py;
            }

            // Draw each section's strip with the correct color
            for (int s = 0; s < SECTIONS; s++) {
                if (sectionTop[s] > sectionBottom[s]) continue;
                int colour = (s == index) ? highlightColour : dimColour;
                graphics.fill(px, sectionTop[s], px + 1, sectionBottom[s] + 1, colour);
            }
        }
    }

    /**
     * Returns true if the given angle falls within [startAngle, endAngle].
     * Handles wraparound across the -PI/PI boundary.
     */
    private boolean isAngleInWedge(float angle, float startAngle, float endAngle) {
        // Normalize all angles to [0, TWO_PI)
        float a = ((angle % TWO_PI) + TWO_PI) % TWO_PI;
        float start = ((startAngle % TWO_PI) + TWO_PI) % TWO_PI;
        float end = ((endAngle % TWO_PI) + TWO_PI) % TWO_PI;

        if (start >= end) {
            return a <= start && a >= end;
        } else {
            // Wedge crosses the 0/TWO_PI boundary
            return a <= start || a >= end;
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        spinButton.active = !spinning && !onCooldown;

        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        if (highlightAlpha > 0 && !lastTierName.isEmpty()) {
            int nameColour = (highlightAlpha << 24) | (lastTierColour & 0x00FFFFFF);
            int x = width / 2 + WHEEL_OFFSET_X;
            int y = height / 2 + WHEEL_OFFSET_Y;
            float scale = 1.5f; // adjust this — 1.0 = normal, 2.0 = double size

            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0);
            graphics.pose().scale(scale, scale, 1f);
            graphics.drawCenteredString(font, lastTierName, 0, 0, nameColour);
            graphics.pose().popPose();
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        // Intentionally empty — suppresses both the title and inventory label
    }
}