package com.navesdev.grimoireknights.progression.network;

import com.navesdev.grimoireknights.core.Ids;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client to server: buy the next rank of a skill tree node. */
public record BuyNodePayload(ResourceLocation node) implements CustomPacketPayload {
    public static final Type<BuyNodePayload> TYPE = new Type<>(Ids.id("buy_node"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BuyNodePayload> STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.<RegistryFriendlyByteBuf>cast().map(BuyNodePayload::new, BuyNodePayload::node);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
