package com.navesdev.grimoireknights.grimoire.network;

import com.navesdev.grimoireknights.core.Ids;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client to server: the element picked in the free choice. The server checks a choice is pending. */
public record ChooseElementPayload(ResourceLocation element) implements CustomPacketPayload {
    public static final Type<ChooseElementPayload> TYPE = new Type<>(Ids.id("choose_element"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChooseElementPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ChooseElementPayload::element,
            ChooseElementPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
