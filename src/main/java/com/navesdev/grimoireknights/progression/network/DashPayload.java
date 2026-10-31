package com.navesdev.grimoireknights.progression.network;

import com.navesdev.grimoireknights.core.Ids;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client to server: dash toward the movement keys, as a world-space horizontal direction (zero = toward the look). */
public record DashPayload(float x, float z) implements CustomPacketPayload {
    public static final Type<DashPayload> TYPE = new Type<>(Ids.id("dash"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DashPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, DashPayload::x,
            ByteBufCodecs.FLOAT, DashPayload::z,
            DashPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
