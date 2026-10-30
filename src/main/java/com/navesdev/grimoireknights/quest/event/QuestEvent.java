package com.navesdev.grimoireknights.quest.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** Posted on {@code NeoForge.EVENT_BUS}, server side. The feature that owns a quest's special behavior listens. */
public abstract class QuestEvent extends Event {
    private final ServerPlayer player;
    private final ResourceLocation quest;

    protected QuestEvent(ServerPlayer player, ResourceLocation quest) {
        this.player = player;
        this.quest = quest;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ResourceLocation getQuest() {
        return quest;
    }
}
