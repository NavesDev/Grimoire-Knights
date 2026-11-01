package com.navesdev.grimoireknights.quest.client;

import com.navesdev.grimoireknights.quest.Quest;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** The tracked quest in the top right corner (approved mockup): title and each objective with its progress. */
final class QuestTrackerHud {
    private static final int WIDTH = 160;
    private static final int MARGIN = 6;

    private QuestTrackerHud() {}

    static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || minecraft.getDebugOverlay().showDebugScreen()) return;
        Optional<ResourceLocation> id = QuestClient.tracked();
        Optional<Quest> quest = id.flatMap(QuestClient::quest);
        if (quest.isEmpty()) return;
        List<Component> lines = QuestClient.objectives(quest.get(), QuestClient.data().active().getOrDefault(id.get(), List.of()));
        int x = graphics.guiWidth() - WIDTH - MARGIN;
        int y = MARGIN + 20;
        int height = 14 + lines.stream().mapToInt(line -> minecraft.font.wordWrapHeight(line, WIDTH - 8)).sum();
        graphics.fill(x - 4, y - 4, x + WIDTH, y + height, 0x90000000);
        graphics.drawString(minecraft.font, minecraft.font.plainSubstrByWidth(Quest.title(id.get()).getString(), WIDTH - 8), x, y, 0xFFF3D989);
        y += 12;
        for (Component line : lines) {
            graphics.drawWordWrap(minecraft.font, line, x, y, WIDTH - 8, 0xFFFFFFFF);
            y += minecraft.font.wordWrapHeight(line, WIDTH - 8);
        }
    }
}
