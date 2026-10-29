package com.navesdev.grimoireknights.grimoire.network;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.network.ClientSink;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server to client: play a playerAnimator pose on a player (render proof, {@code /gk anim}). */
public record PlayerAnimationPayload(int entityId, ResourceLocation animation) implements CustomPacketPayload {
    public static final Type<PlayerAnimationPayload> TYPE = new Type<>(Ids.id("player_animation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerAnimationPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PlayerAnimationPayload::entityId,
            ResourceLocation.STREAM_CODEC, PlayerAnimationPayload::animation,
            PlayerAnimationPayload::new);
    public static final ClientSink<PlayerAnimationPayload> CLIENT = new ClientSink<>();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
