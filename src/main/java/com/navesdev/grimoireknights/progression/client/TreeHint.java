package com.navesdev.grimoireknights.progression.client;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.client.KeyHint;
import com.navesdev.grimoireknights.progression.SkillTreeService;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Hint for points to spend: shows when the player has more points than when they last opened the tree. */
final class TreeHint {
    private static int seen;

    private TreeHint() {}

    static int points() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? 0 : SkillTreeService.data(minecraft.player).points().values().stream().mapToInt(Integer::intValue).sum();
    }

    /** The tree was opened: the current points are known. */
    static void seen() {
        seen = points();
    }

    static void render(GuiGraphics graphics, DeltaTracker delta) {
        int points = points();
        seen = Math.min(seen, points);
        if (points <= seen) return;
        KeyHint.draw(graphics, 0, Component.translatable(Ids.key("hud", "hint.points"), points), ProgressionClientModule.TREE,
                Component.translatable(Ids.key("hud", "hint.open_tree")));
    }
}
