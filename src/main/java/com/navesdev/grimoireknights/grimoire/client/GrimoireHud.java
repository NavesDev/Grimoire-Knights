package com.navesdev.grimoireknights.grimoire.client;

import com.navesdev.grimoireknights.grimoire.GrimoireModule;
import com.navesdev.grimoireknights.grimoire.attachment.GrimoireData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

/** The player's grimoire, always on screen after the ceremony: left of the mana bar and slot Z (ManaHud). */
final class GrimoireHud {
    private static final int SIZE = 24;
    /** Past ManaHud's slot (hotbar half 91, offhand 29, gap 6, slot 20) with a 4 pixel gap. */
    private static final int FROM_CENTER = 91 + 29 + 6 + 20 + 4 + SIZE;
    private static ItemStack icon = ItemStack.EMPTY;

    private GrimoireHud() {}

    static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.options.hideGui || player.isSpectator()) return;
        GrimoireData data = player.getData(GrimoireModule.GRIMOIRE_DATA);
        if (!data.hasGrimoire() || data.element().isEmpty()) return;
        String cover = data.element().get().getPath() + "_" + data.leaves();
        if (!cover.equals(icon.get(GrimoireModule.GRIMOIRE_COVER.get()))) {
            icon = new ItemStack(GrimoireModule.GRIMOIRE_ICON.get());
            icon.set(GrimoireModule.GRIMOIRE_COVER.get(), cover);
        }
        int x = graphics.guiWidth() / 2 - FROM_CENTER;
        int y = graphics.guiHeight() - SIZE - 2;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(SIZE / 16f, SIZE / 16f, 1);
        graphics.renderItem(icon, 0, 0);
        graphics.pose().popPose();
    }
}
