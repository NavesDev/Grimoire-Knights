package com.navesdev.grimoireknights.progression.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/** Level and progress of each mastery; a missing mastery is level 1 with no progress. Immutable: change with {@code setData}. */
public record MasteryData(Map<ResourceLocation, Entry> masteries) {
    public static final MasteryData EMPTY = new MasteryData(Map.of());

    public record Entry(int level, double progress) {
        public static final Entry START = new Entry(1, 0);
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("level").forGetter(Entry::level),
                Codec.DOUBLE.optionalFieldOf("progress", 0.0).forGetter(Entry::progress)
        ).apply(instance, Entry::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Entry::level,
                ByteBufCodecs.DOUBLE, Entry::progress,
                Entry::new);
    }

    public static final Codec<MasteryData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(ResourceLocation.CODEC, Entry.CODEC).optionalFieldOf("masteries", Map.of()).forGetter(MasteryData::masteries)
    ).apply(instance, MasteryData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MasteryData> STREAM_CODEC = ByteBufCodecs
            .<RegistryFriendlyByteBuf, ResourceLocation, Entry, Map<ResourceLocation, Entry>>map(HashMap::new, ResourceLocation.STREAM_CODEC, Entry.STREAM_CODEC)
            .map(MasteryData::new, MasteryData::masteries);

    public MasteryData {
        masteries = Map.copyOf(masteries);
    }

    public Entry get(ResourceLocation mastery) {
        return masteries.getOrDefault(mastery, Entry.START);
    }

    public MasteryData with(ResourceLocation mastery, Entry entry) {
        Map<ResourceLocation, Entry> next = new HashMap<>(masteries);
        next.put(mastery, entry);
        return new MasteryData(next);
    }
}
