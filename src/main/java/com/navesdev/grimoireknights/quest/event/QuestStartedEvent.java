package com.navesdev.grimoireknights.quest.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class QuestStartedEvent extends QuestEvent {
    public QuestStartedEvent(ServerPlayer player, ResourceLocation quest) {
        super(player, quest);
    }
}
