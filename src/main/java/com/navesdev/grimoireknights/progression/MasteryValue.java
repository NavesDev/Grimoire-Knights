package com.navesdev.grimoireknights.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Entry of the {@code grimoireknights:mastery} data map on entity types: mastery a kill is worth. */
public record MasteryValue(double amount) {
    public static final Codec<MasteryValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.doubleRange(0, 1e6).fieldOf("amount").forGetter(MasteryValue::amount)
    ).apply(instance, MasteryValue::new));
}
