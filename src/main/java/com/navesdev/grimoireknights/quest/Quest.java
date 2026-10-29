package com.navesdev.grimoireknights.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

/** A quest, loaded from the {@code grimoireknights:quest} datapack registry. Special behavior hangs on the quest events. */
public record Quest(List<Objective> objectives, List<Reward> rewards, List<ResourceLocation> requires, boolean repeatable, Source source) {
    public static final Codec<Quest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Objective.CODEC.listOf().optionalFieldOf("objectives", List.of()).forGetter(Quest::objectives),
            Reward.CODEC.listOf().optionalFieldOf("rewards", List.of()).forGetter(Quest::rewards),
            ResourceLocation.CODEC.listOf().optionalFieldOf("requires", List.of()).forGetter(Quest::requires),
            Codec.BOOL.optionalFieldOf("repeatable", false).forGetter(Quest::repeatable),
            Source.CODEC.optionalFieldOf("source", Source.STORY).forGetter(Quest::source)
    ).apply(instance, Quest::new));

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
