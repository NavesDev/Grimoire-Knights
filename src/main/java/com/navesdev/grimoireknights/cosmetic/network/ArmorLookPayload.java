package com.navesdev.grimoireknights.cosmetic.network;

import com.navesdev.grimoireknights.core.Ids;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client to server: hide or show the sender's armor. */
public record ArmorLookPayload(boolean hidden) implements CustomPacketPayload {
    public static final Type<ArmorLookPayload> TYPE = new Type<>(Ids.id("armor_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorLookPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ArmorLookPayload::hidden,
            ArmorLookPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
