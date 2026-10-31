package com.navesdev.grimoireknights.ability.network;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.network.ClientSink;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server to client: the owner's Tailwind stacks, for the HUD. */
public record TailwindPayload(int stacks) implements CustomPacketPayload {
    public static final Type<TailwindPayload> TYPE = new Type<>(Ids.id("tailwind"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TailwindPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.<RegistryFriendlyByteBuf>cast().map(TailwindPayload::new, TailwindPayload::stacks);
    public static final ClientSink<TailwindPayload> CLIENT = new ClientSink<>();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
