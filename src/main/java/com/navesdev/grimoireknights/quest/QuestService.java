package com.navesdev.grimoireknights.quest;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.quest.attachment.QuestData;
import com.navesdev.grimoireknights.quest.event.QuestCompletedEvent;
import com.navesdev.grimoireknights.quest.event.QuestFailedEvent;
import com.navesdev.grimoireknights.quest.event.QuestStartedEvent;
import com.navesdev.grimoireknights.quest.rules.QuestRules;
import com.navesdev.grimoireknights.quest.network.DialoguePayload;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.ToIntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

/** Starts, completes and fails quests on the server; the engine knows no quest in particular. */
public final class QuestService {
    public enum GiveResult { OK, UNKNOWN_QUEST, ALREADY_ACTIVE, ALREADY_COMPLETED, MISSING_PREREQUISITE }

    private static final Map<UUID, ResourceLocation> OFFERED = new HashMap<>();

    private QuestService() {}

    public static Optional<Quest> find(MinecraftServer server, ResourceLocation id) {
        return server.registryAccess().registryOrThrow(QuestRegistries.QUEST).getOptional(id);
    }

    public static Stream<ResourceLocation> ids(MinecraftServer server) {
        return server.registryAccess().registryOrThrow(QuestRegistries.QUEST).keySet().stream();
    }

    public static QuestData data(net.minecraft.world.entity.player.Player player) {
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

    /** Adds each objective's gain to every active quest; a quest whose objectives are all done completes and pays. */
    public static void advance(ServerPlayer player, ToIntFunction<Objective> gain) {
        QuestData data = data(player);
        for (Map.Entry<ResourceLocation, List<Integer>> active : data.active().entrySet()) {
            Optional<Quest> quest = find(player.server, active.getKey());
            if (quest.isEmpty() || active.getValue().size() != quest.get().objectives().size()) continue;
            List<Integer> gains = quest.get().objectives().stream().map(gain::applyAsInt).toList();
            if (gains.stream().allMatch(g -> g <= 0)) continue;
            List<Integer> next = QuestRules.advance(active.getValue(), quest.get().required(), gains);
            player.setData(QuestModule.QUEST_DATA, data(player).withProgress(active.getKey(), next));
            if (QuestRules.done(next, quest.get().required())) complete(player, active.getKey());
        }
    }

    /** The next quest this NPC type offers the player: the first one, by id, that can start now. */
    public static Optional<ResourceLocation> offerFrom(ServerPlayer player, ResourceLocation npc) {
        QuestData data = data(player);
        return ids(player.server).sorted().filter(id -> find(player.server, id).filter(quest -> quest.giver().equals(Optional.of(npc))
                && QuestRules.canStart(id, data.active().keySet(), data.completed(), quest.requires(), quest.repeatable()) == QuestRules.StartResult.OK)
                .isPresent()).findFirst();
    }

    /** Shows an NPC line in the dialogue screen; with a quest, the player may accept it (only that one, until the next line). */
    public static void say(ServerPlayer player, Entity npc, Component text, Optional<ResourceLocation> quest) {
        if (quest.isPresent()) OFFERED.put(player.getUUID(), quest.get());
        else OFFERED.remove(player.getUUID());
        PacketDistributor.sendToPlayer(player, new DialoguePayload(npc.getId(), text, quest));
    }

    /** Talking to an NPC counts for talk objectives. */
    public static void talked(ServerPlayer player, Entity npc) {
        advance(player, objective -> objective.onTalk(player, npc.getType()));
    }

    /** Accepting gives only the quest the NPC offered last; anything else is ignored. */
    public static GiveResult answer(ServerPlayer player, ResourceLocation id, boolean accept) {
        ResourceLocation offered = OFFERED.remove(player.getUUID());
        if (!id.equals(offered)) {
            GrimoireKnights.LOGGER.debug("Quest answer for {} from {} ignored: not the offered quest", id, player.getGameProfile().getName());
            return GiveResult.UNKNOWN_QUEST;
        }
        return accept ? give(player, id) : GiveResult.OK;
    }

    /** Test command: completes and pays a quest, starting it first when needed (prerequisites skipped). */
    public static boolean forceComplete(ServerPlayer player, ResourceLocation id) {
        Optional<Quest> quest = find(player.server, id);
        if (quest.isEmpty()) return false;
        if (!data(player).active().containsKey(id)) {
            player.setData(QuestModule.QUEST_DATA, data(player).start(id, quest.get().objectives().size()));
            NeoForge.EVENT_BUS.post(new QuestStartedEvent(player, id));
        }
        return complete(player, id);
    }

    public static void forget(ServerPlayer player) {
        OFFERED.remove(player.getUUID());
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
