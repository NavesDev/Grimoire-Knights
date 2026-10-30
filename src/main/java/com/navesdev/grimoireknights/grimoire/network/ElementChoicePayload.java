package com.navesdev.grimoireknights.grimoire.network;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.network.ClientSink;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server to client: free choice is on, so the player picks the element among these. */
public record ElementChoicePayload(List<ResourceLocation> elements) implements CustomPacketPayload {
    public static final Type<ElementChoicePayload> TYPE = new Type<>(Ids.id("element_choice"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ElementChoicePayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), ElementChoicePayload::elements,
            ElementChoicePayload::new);
    public static final ClientSink<ElementChoicePayload> CLIENT = new ClientSink<>();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
