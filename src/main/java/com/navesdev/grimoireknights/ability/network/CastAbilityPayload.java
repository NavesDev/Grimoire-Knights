package com.navesdev.grimoireknights.ability.network;

import com.navesdev.grimoireknights.core.Ids;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client to server: the player pressed the key of {@code slot} (0 = Z). The server checks everything. */
public record CastAbilityPayload(int slot) implements CustomPacketPayload {
    public static final Type<CastAbilityPayload> TYPE = new Type<>(Ids.id("cast_ability"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastAbilityPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CastAbilityPayload::slot,
            CastAbilityPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
