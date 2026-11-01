package com.navesdev.grimoireknights.quest.client;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.client.KeyHint;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Hint for a new quest: shows while an active quest was never seen in the journal. */
final class JournalHint {
    private static final Set<ResourceLocation> SEEN = new HashSet<>();

    private JournalHint() {}

    /** The journal was opened: every active quest is known. */
    static void seen() {
        SEEN.addAll(QuestClient.data().active().keySet());
    }

    static void render(GuiGraphics graphics, DeltaTracker delta) {
        if (SEEN.containsAll(QuestClient.data().active().keySet())) return;
        KeyHint.draw(graphics, 1, Component.translatable(Ids.key("hud", "hint.new_quest")), QuestClientModule.JOURNAL,
                Component.translatable(Ids.key("hud", "hint.open_journal")));
    }
}
