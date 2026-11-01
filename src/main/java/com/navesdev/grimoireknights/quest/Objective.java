package com.navesdev.grimoireknights.quest;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/** A quest objective. One class per type, registered in {@code grimoireknights:objective_type} by its owning feature. */
public interface Objective {
    Codec<Objective> CODEC = Codec.lazyInitialized(
            () -> QuestRegistries.OBJECTIVE_TYPES.byNameCodec().dispatch(Objective::type, ObjectiveType::codec));

    ObjectiveType<?> type();

    /** Progress needed to complete this objective. */
    int required();

    /** Shown in the dialogue, the journal and the tracker. */
    Component describe();

    /** Progress gained when the player kills this entity. */
    default int onKill(ServerPlayer player, LivingEntity killed) {
        return 0;
    }

    /** Progress gained when the player talks to an NPC of this type. */
    default int onTalk(ServerPlayer player, EntityType<?> npc) {
        return 0;
    }

    /** Checked every second: true completes a place objective. */
    default boolean reached(ServerPlayer player) {
        return false;
    }
}
