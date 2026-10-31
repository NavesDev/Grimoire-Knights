package com.navesdev.grimoireknights.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

/**
 * A quest, loaded from the {@code grimoireknights:quest} datapack registry (synced to clients for the screens). {@code giver}
 * is the entity type of the NPC that offers it. Texts come from {@code quest.<ns>.<path>.title|offer}. Special behavior hangs on the quest events.
 */
public record Quest(List<Objective> objectives, List<Reward> rewards, List<ResourceLocation> requires, boolean repeatable, Source source,
                    Optional<ResourceLocation> giver) {
    public static final Codec<Quest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Objective.CODEC.listOf().optionalFieldOf("objectives", List.of()).forGetter(Quest::objectives),
            Reward.CODEC.listOf().optionalFieldOf("rewards", List.of()).forGetter(Quest::rewards),
            ResourceLocation.CODEC.listOf().optionalFieldOf("requires", List.of()).forGetter(Quest::requires),
            Codec.BOOL.optionalFieldOf("repeatable", false).forGetter(Quest::repeatable),
            Source.CODEC.optionalFieldOf("source", Source.STORY).forGetter(Quest::source),
            ResourceLocation.CODEC.optionalFieldOf("giver").forGetter(Quest::giver)
    ).apply(instance, Quest::new));

    public List<Integer> required() {
        return objectives.stream().map(Objective::required).toList();
    }

    public static Component title(ResourceLocation id) {
        return Component.translatable(key(id, "title"));
    }

    /** What the giver says when offering it. */
    public static Component offer(ResourceLocation id) {
        return Component.translatable(key(id, "offer"));
    }

    private static String key(ResourceLocation id, String part) {
        return "quest." + id.getNamespace() + "." + id.getPath().replace('/', '.') + "." + part;
    }

    /** Where the quest is offered. */
    public enum Source implements StringRepresentable {
        STORY("story"),
        BOARD("board");

        public static final Codec<Source> CODEC = StringRepresentable.fromEnum(Source::values);
        private final String name;

        Source(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
