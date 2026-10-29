package com.navesdev.grimoireknights.grimoire.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * The player's grimoire: element (empty = no grimoire), clover leaves, whether the ceremony is done and the calling bar.
 * Immutable: change with {@code setData}, which also syncs it.
 */
public record GrimoireData(Optional<ResourceLocation> element, int leaves, boolean ceremonyDone, double calling) {
    public static final GrimoireData EMPTY = new GrimoireData(Optional.empty(), 0, false, 0);

    public static final Codec<GrimoireData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("element").forGetter(GrimoireData::element),
            Codec.INT.optionalFieldOf("leaves", 0).forGetter(GrimoireData::leaves),
            Codec.BOOL.optionalFieldOf("ceremony_done", false).forGetter(GrimoireData::ceremonyDone),
            Codec.DOUBLE.optionalFieldOf("calling", 0.0).forGetter(GrimoireData::calling)
    ).apply(instance, GrimoireData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GrimoireData> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional), GrimoireData::element,
            ByteBufCodecs.VAR_INT, GrimoireData::leaves,
            ByteBufCodecs.BOOL, GrimoireData::ceremonyDone,
            ByteBufCodecs.DOUBLE, GrimoireData::calling,
            GrimoireData::new);

    public boolean hasGrimoire() {
        return element.isPresent();
    }

    public GrimoireData withGrimoire(ResourceLocation newElement, int newLeaves) {
        return new GrimoireData(Optional.of(newElement), newLeaves, true, calling);
    }

    /** Ceremony done without a grimoire: the "no magic" path. */
    public GrimoireData withoutMagic() {
        return new GrimoireData(Optional.empty(), 0, true, calling);
    }

    public GrimoireData cleared() {
        return new GrimoireData(Optional.empty(), 0, false, calling);
    }

    public GrimoireData withCalling(double value) {
        return new GrimoireData(element, leaves, ceremonyDone, value);
    }
}
