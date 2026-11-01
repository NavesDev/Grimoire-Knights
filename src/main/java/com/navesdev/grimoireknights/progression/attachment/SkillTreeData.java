package com.navesdev.grimoireknights.progression.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/** Unspent points per mastery and the rank of each bought node. Immutable: change with {@code setData}. */
public record SkillTreeData(Map<ResourceLocation, Integer> points, Map<ResourceLocation, Integer> nodes) {
    public static final SkillTreeData EMPTY = new SkillTreeData(Map.of(), Map.of());

    private static final StreamCodec<RegistryFriendlyByteBuf, Map<ResourceLocation, Integer>> MAP_STREAM =
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT);

    public static final Codec<SkillTreeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).optionalFieldOf("points", Map.of()).forGetter(SkillTreeData::points),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).optionalFieldOf("nodes", Map.of()).forGetter(SkillTreeData::nodes)
    ).apply(instance, SkillTreeData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillTreeData> STREAM_CODEC = StreamCodec.composite(
            MAP_STREAM, SkillTreeData::points,
            MAP_STREAM, SkillTreeData::nodes,
            SkillTreeData::new);

    public SkillTreeData {
        points = Map.copyOf(points);
        nodes = Map.copyOf(nodes);
    }

    public int points(ResourceLocation mastery) {
        return points.getOrDefault(mastery, 0);
    }

    public int rank(ResourceLocation node) {
        return nodes.getOrDefault(node, 0);
    }

    public SkillTreeData withPoints(ResourceLocation mastery, int value) {
        Map<ResourceLocation, Integer> next = new HashMap<>(points);
        next.put(mastery, Math.max(0, value));
        return new SkillTreeData(next, nodes);
    }

    public SkillTreeData withNode(ResourceLocation node, int rank) {
        Map<ResourceLocation, Integer> next = new HashMap<>(nodes);
        if (rank <= 0) next.remove(node);
        else next.put(node, rank);
        return new SkillTreeData(points, next);
    }
}
