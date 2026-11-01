package com.navesdev.grimoireknights.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Entry of the {@code grimoireknights:combat_level} data map on entity types: the level a mob hits with. */
public record CombatLevel(int level) {
    public static final Codec<CombatLevel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 1000).fieldOf("level").forGetter(CombatLevel::level)
    ).apply(instance, CombatLevel::new));
}
