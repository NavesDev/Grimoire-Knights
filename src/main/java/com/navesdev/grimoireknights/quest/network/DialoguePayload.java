package com.navesdev.grimoireknights.quest.network;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.network.ClientSink;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server to client: an NPC line for the dialogue screen; with {@code quest}, the screen offers it (accept or decline). */
public record DialoguePayload(int npc, Component text, Optional<ResourceLocation> quest) implements CustomPacketPayload {
    public static final Type<DialoguePayload> TYPE = new Type<>(Ids.id("dialogue"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DialoguePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DialoguePayload::npc,
            ComponentSerialization.TRUSTED_STREAM_CODEC, DialoguePayload::text,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional), DialoguePayload::quest,
            DialoguePayload::new);
    public static final ClientSink<DialoguePayload> CLIENT = new ClientSink<>();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
