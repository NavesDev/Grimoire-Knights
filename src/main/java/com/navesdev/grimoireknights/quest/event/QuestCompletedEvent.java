package com.navesdev.grimoireknights.quest.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class QuestCompletedEvent extends QuestEvent {
    public QuestCompletedEvent(ServerPlayer player, ResourceLocation quest) {
        super(player, quest);
    }
}
