package com.navesdev.grimoireknights.quest.network;

import com.navesdev.grimoireknights.core.Ids;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client to server: accept or decline the quest the NPC just offered. */
public record QuestAnswerPayload(ResourceLocation quest, boolean accept) implements CustomPacketPayload {
    public static final Type<QuestAnswerPayload> TYPE = new Type<>(Ids.id("quest_answer"));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuestAnswerPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestAnswerPayload::quest,
            ByteBufCodecs.BOOL, QuestAnswerPayload::accept,
            QuestAnswerPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
