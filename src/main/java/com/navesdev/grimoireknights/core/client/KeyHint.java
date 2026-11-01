package com.navesdev.grimoireknights.core.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * A small, quiet chip in the top left corner that points to a screen's key when it has news (approved mockup, option
 * B; moved off the spell slots after playtest): "✦ text [K] action". Each feature decides when to show it and hides it
 * once its screen is opened.
 */
public final class KeyHint {
    private static final int FILL = 0x90000000;
    private static final int EDGE = 0x80404040;
    private static final int KEY = 0xFF333333;
    private static final int STAR = 0xFFBDBDBD;
    private static final int TEXT = 0xFFE0E0E0;
    private static final int MUTED = 0xFF9A9A9A;
    private static final int MARGIN = 4;
    private static final int HEIGHT = 13;
    /** Drawn at three quarters of the GUI scale, to stay discreet. */
    private static final float SCALE = 0.75f;

    private KeyHint() {}

    /** @param slot 0 for the top chip; higher slots stack below it */
    public static void draw(GuiGraphics graphics, int slot, Component news, KeyMapping key, Component action) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.getDebugOverlay().showDebugScreen() || minecraft.screen != null) return;
        var font = minecraft.font;
        String star = "✦ ";
        String keyName = key.getTranslatedKeyMessage().getString();
        int x = MARGIN;
        int y = MARGIN + slot * (HEIGHT + 2);
        int width = font.width(star) + font.width(news) + 6 + font.width(keyName) + 6 + font.width(action) + 9;
        graphics.pose().pushPose();
        graphics.pose().scale(SCALE, SCALE, 1);
        graphics.fill(x + 1, y, x + width - 1, y + HEIGHT, EDGE);
        graphics.fill(x, y + 1, x + width, y + HEIGHT - 1, EDGE);
        graphics.fill(x + 1, y + 1, x + width - 1, y + HEIGHT - 1, FILL);
        int cx = x + 4;
        graphics.drawString(font, star, cx, y + 3, STAR, false);
        cx += font.width(star);
        graphics.drawString(font, news, cx, y + 3, TEXT, false);
        cx += font.width(news) + 6;
        graphics.fill(cx - 2, y + 2, cx + font.width(keyName) + 2, y + HEIGHT - 2, KEY);
        graphics.drawString(font, keyName, cx, y + 3, TEXT, false);
        cx += font.width(keyName) + 6;
        graphics.drawString(font, action, cx, y + 3, MUTED, false);
        graphics.pose().popPose();
    }
}
