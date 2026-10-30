package com.navesdev.grimoireknights.quest.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/** Active quests (progress per objective) and completed quests. Immutable: change with {@code setData}. */
public record QuestData(Map<ResourceLocation, List<Integer>> active, Set<ResourceLocation> completed) {
    public static final QuestData EMPTY = new QuestData(Map.of(), Set.of());

    public static final Codec<QuestData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT.listOf()).optionalFieldOf("active", Map.of()).forGetter(QuestData::active),
            ResourceLocation.CODEC.listOf().<Set<ResourceLocation>>xmap(Set::copyOf, List::copyOf)
                    .optionalFieldOf("completed", Set.of()).forGetter(QuestData::completed)
    ).apply(instance, QuestData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.<RegistryFriendlyByteBuf, ResourceLocation, List<Integer>, Map<ResourceLocation, List<Integer>>>map(
                    HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list())),
            QuestData::active,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new)),
            QuestData::completed,
            QuestData::new);

    public QuestData {
        active = Map.copyOf(active);
        completed = Set.copyOf(completed);
    }

    public QuestData start(ResourceLocation quest, int objectives) {
        Map<ResourceLocation, List<Integer>> next = new HashMap<>(active);
        next.put(quest, java.util.Collections.nCopies(objectives, 0));
        return new QuestData(next, completed);
    }

    public QuestData complete(ResourceLocation quest) {
        Set<ResourceLocation> done = new HashSet<>(completed);
        done.add(quest);
        return new QuestData(without(quest), done);
    }

    public QuestData drop(ResourceLocation quest) {
        return new QuestData(without(quest), completed);
    }

    public QuestData forget(ResourceLocation quest) {
        Set<ResourceLocation> done = new HashSet<>(completed);
        done.remove(quest);
        return new QuestData(without(quest), done);
    }

    private Map<ResourceLocation, List<Integer>> without(ResourceLocation quest) {
        Map<ResourceLocation, List<Integer>> next = new HashMap<>(active);
        next.remove(quest);
        return next;
    }
}
