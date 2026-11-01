package com.navesdev.grimoireknights.progression.network;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.network.ClientSink;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server to client: a dash happened; the HUD darkens the dash icon for {@code ticks}. */
public record DashCooldownPayload(int ticks) implements CustomPacketPayload {
    public static final Type<DashCooldownPayload> TYPE = new Type<>(Ids.id("dash_cooldown"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DashCooldownPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.<RegistryFriendlyByteBuf>cast().map(DashCooldownPayload::new, DashCooldownPayload::ticks);
    public static final ClientSink<DashCooldownPayload> CLIENT = new ClientSink<>();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
