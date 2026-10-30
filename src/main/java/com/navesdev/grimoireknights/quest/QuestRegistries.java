package com.navesdev.grimoireknights.quest;

import com.navesdev.grimoireknights.core.registry.CoreRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class QuestRegistries {
    public static final ResourceKey<Registry<Quest>> QUEST = CoreRegistries.key("quest");
    public static final ResourceKey<Registry<ObjectiveType<?>>> OBJECTIVE_TYPE = CoreRegistries.key("objective_type");
    public static final ResourceKey<Registry<RewardType<?>>> REWARD_TYPE = CoreRegistries.key("reward_type");

    public static final Registry<ObjectiveType<?>> OBJECTIVE_TYPES = CoreRegistries.local(OBJECTIVE_TYPE);
    public static final Registry<RewardType<?>> REWARD_TYPES = CoreRegistries.local(REWARD_TYPE);

    private QuestRegistries() {}
}
