package com.navesdev.grimoireknights.quest.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class QuestFailedEvent extends QuestEvent {
    public QuestFailedEvent(ServerPlayer player, ResourceLocation quest) {
        super(player, quest);
    }
}
