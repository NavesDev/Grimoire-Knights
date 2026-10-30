package com.navesdev.grimoireknights.core.vfx;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.network.ClientSink;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server to client: play a Blockbench effect sequence at a spot, with Molang variables ({@code variable.<name>}). */
public record VfxPayload(ResourceLocation effect, double x, double y, double z, Map<String, Float> variables) implements CustomPacketPayload {
    public static final Type<VfxPayload> TYPE = new Type<>(Ids.id("vfx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VfxPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, VfxPayload::effect,
            ByteBufCodecs.DOUBLE, VfxPayload::x,
            ByteBufCodecs.DOUBLE, VfxPayload::y,
            ByteBufCodecs.DOUBLE, VfxPayload::z,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.FLOAT, 16), VfxPayload::variables,
            VfxPayload::new);
    public static final ClientSink<VfxPayload> CLIENT = new ClientSink<>();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
