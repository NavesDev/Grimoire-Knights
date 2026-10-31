package com.navesdev.grimoireknights.quest.client;

import com.navesdev.grimoireknights.quest.Objective;
import com.navesdev.grimoireknights.quest.Quest;
import com.navesdev.grimoireknights.quest.QuestModule;
import com.navesdev.grimoireknights.quest.QuestRegistries;
import com.navesdev.grimoireknights.quest.attachment.QuestData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Client view of the quests: the synced registry and data, and which active quest the HUD tracks. */
final class QuestClient {
    /** Empty = follow the first active quest; {@code untracked} hides the tracker (the journal button). */
    private static Optional<ResourceLocation> tracked = Optional.empty();
    private static boolean untracked;

    private QuestClient() {}

    static Optional<Quest> quest(ResourceLocation id) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return Optional.empty();
        return minecraft.level.registryAccess().registry(QuestRegistries.QUEST).flatMap(registry -> registry.getOptional(id));
    }

    static QuestData data() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? QuestData.EMPTY : minecraft.player.getData(QuestModule.QUEST_DATA);
    }

    static Optional<ResourceLocation> tracked() {
        if (untracked) return Optional.empty();
        QuestData data = data();
        return tracked.filter(data.active()::containsKey).or(() -> data.active().keySet().stream().sorted().findFirst());
    }

    static boolean isTracked(ResourceLocation id) {
        return tracked().equals(Optional.of(id));
    }

    static void toggle(ResourceLocation id) {
        if (isTracked(id)) {
            untracked = true;
        } else {
            tracked = Optional.of(id);
            untracked = false;
        }
    }

    /** "• objective 0/1" lines, done ones in green. */
    static List<Component> objectives(Quest quest, List<Integer> progress) {
        List<Component> lines = new ArrayList<>();
        for (int i = 0; i < quest.objectives().size(); i++) {
            Objective objective = quest.objectives().get(i);
            int done = i < progress.size() ? progress.get(i) : objective.required();
            lines.add(Component.literal("• ").append(objective.describe()).append(" " + done + "/" + objective.required())
                    .withStyle(done >= objective.required() ? ChatFormatting.GREEN : ChatFormatting.WHITE));
        }
        return lines;
    }
}
