package com.navesdev.grimoireknights.grimoire.network;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.network.ClientSink;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server to client: a player with a grimoire is fighting or cast magic, so their floating grimoire shows. */
public record GrimoireActivityPayload(int entityId, Kind kind) implements CustomPacketPayload {
    public static final Type<GrimoireActivityPayload> TYPE = new Type<>(Ids.id("grimoire_activity"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GrimoireActivityPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, GrimoireActivityPayload::entityId,
            Kind.STREAM_CODEC, GrimoireActivityPayload::kind,
            GrimoireActivityPayload::new);
    public static final ClientSink<GrimoireActivityPayload> CLIENT = new ClientSink<>();

    public enum Kind {
        COMBAT, CAST;

        static final StreamCodec<ByteBuf, Kind> STREAM_CODEC = ByteBufCodecs.idMapper(i -> values()[i], Kind::ordinal);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
