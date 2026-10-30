package com.navesdev.grimoireknights.quest;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;

/** A quest reward. One class per type, registered in {@code grimoireknights:reward_type} by its owning feature. */
public interface Reward {
    Codec<Reward> CODEC = Codec.lazyInitialized(
            () -> QuestRegistries.REWARD_TYPES.byNameCodec().dispatch(Reward::type, RewardType::codec));

    RewardType<?> type();

    void grant(ServerPlayer player);
}
