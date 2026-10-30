package com.navesdev.grimoireknights.quest;

import com.mojang.serialization.Codec;

/** A quest objective. One class per type, registered in {@code grimoireknights:objective_type} by its owning feature. */
public interface Objective {
    Codec<Objective> CODEC = Codec.lazyInitialized(
            () -> QuestRegistries.OBJECTIVE_TYPES.byNameCodec().dispatch(Objective::type, ObjectiveType::codec));

    ObjectiveType<?> type();

    /** Progress needed to complete this objective. */
    int required();
}
