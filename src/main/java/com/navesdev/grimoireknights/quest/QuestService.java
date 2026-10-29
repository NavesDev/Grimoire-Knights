package com.navesdev.grimoireknights.quest;

import com.navesdev.grimoireknights.quest.attachment.QuestData;
import com.navesdev.grimoireknights.quest.event.QuestCompletedEvent;
import com.navesdev.grimoireknights.quest.event.QuestFailedEvent;
import com.navesdev.grimoireknights.quest.event.QuestStartedEvent;
import com.navesdev.grimoireknights.quest.rules.QuestRules;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

/** Starts, completes and fails quests on the server; the engine knows no quest in particular. */
public final class QuestService {
    public enum GiveResult { OK, UNKNOWN_QUEST, ALREADY_ACTIVE, ALREADY_COMPLETED, MISSING_PREREQUISITE }

    private QuestService() {}

    public static Optional<Quest> find(MinecraftServer server, ResourceLocation id) {
        return server.registryAccess().registryOrThrow(QuestRegistries.QUEST).getOptional(id);
    }

    public static Stream<ResourceLocation> ids(MinecraftServer server) {
        return server.registryAccess().registryOrThrow(QuestRegistries.QUEST).keySet().stream();
    }

    public static QuestData data(ServerPlayer player) {
        return player.getData(QuestModule.QUEST_DATA);
    }

    public static GiveResult give(ServerPlayer player, ResourceLocation id) {
        Optional<Quest> quest = find(player.server, id);
        if (quest.isEmpty()) return GiveResult.UNKNOWN_QUEST;
        QuestData data = data(player);
        GiveResult result = switch (QuestRules.canStart(id, data.active().keySet(), data.completed(), quest.get().requires(), quest.get().repeatable())) {
            case OK -> GiveResult.OK;
            case ALREADY_ACTIVE -> GiveResult.ALREADY_ACTIVE;
            case ALREADY_COMPLETED -> GiveResult.ALREADY_COMPLETED;
            case MISSING_PREREQUISITE -> GiveResult.MISSING_PREREQUISITE;
        };
        if (result != GiveResult.OK) return result;
        player.setData(QuestModule.QUEST_DATA, data.start(id, quest.get().objectives().size()));
        NeoForge.EVENT_BUS.post(new QuestStartedEvent(player, id));
        return GiveResult.OK;
    }

    /** Pays the rewards and marks the quest done. False when the quest is not active. */
    public static boolean complete(ServerPlayer player, ResourceLocation id) {
        QuestData data = data(player);
        Optional<Quest> quest = find(player.server, id);
        if (!data.active().containsKey(id) || quest.isEmpty()) return false;
        player.setData(QuestModule.QUEST_DATA, data.complete(id));
        quest.get().rewards().forEach(reward -> reward.grant(player));
        NeoForge.EVENT_BUS.post(new QuestCompletedEvent(player, id));
        return true;
    }

    /** Drops an active quest so it can start again. False when the quest is not active. */
    public static boolean fail(ServerPlayer player, ResourceLocation id) {
        QuestData data = data(player);
        if (!data.active().containsKey(id)) return false;
        player.setData(QuestModule.QUEST_DATA, data.drop(id));
        NeoForge.EVENT_BUS.post(new QuestFailedEvent(player, id));
        return true;
    }

    /** Test helper: forgets the quest, active or completed. */
    public static void clear(ServerPlayer player, ResourceLocation id) {
        player.setData(QuestModule.QUEST_DATA, data(player).forget(id));
    }

    public static void clearAll(ServerPlayer player) {
        player.setData(QuestModule.QUEST_DATA, QuestData.EMPTY);
    }
}
