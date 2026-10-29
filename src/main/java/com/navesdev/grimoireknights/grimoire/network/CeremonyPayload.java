package com.navesdev.grimoireknights.grimoire.network;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.network.ClientSink;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server to client: the grimoire comes down from {@code from} (the shelves above the altar) and chooses the player. */
public record CeremonyPayload(int entityId, BlockPos from, ResourceLocation element, int leaves) implements CustomPacketPayload {
    public static final Type<CeremonyPayload> TYPE = new Type<>(Ids.id("ceremony"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CeremonyPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CeremonyPayload::entityId,
            BlockPos.STREAM_CODEC, CeremonyPayload::from,
            ResourceLocation.STREAM_CODEC, CeremonyPayload::element,
            ByteBufCodecs.VAR_INT, CeremonyPayload::leaves,
            CeremonyPayload::new);
    public static final ClientSink<CeremonyPayload> CLIENT = new ClientSink<>();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
