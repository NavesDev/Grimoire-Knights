package com.navesdev.grimoireknights.progression.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.navesdev.grimoireknights.progression.MasteryService;
import com.navesdev.grimoireknights.progression.ProgressionModule;
import com.navesdev.grimoireknights.quest.Reward;
import com.navesdev.grimoireknights.quest.RewardType;
import com.navesdev.grimoireknights.core.Ids;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** {@code mastery}: progress to a mastery; without {@code mastery}, the current element's. */
public record MasteryReward(Optional<ResourceLocation> mastery, double amount) implements Reward {
    public static final MapCodec<MasteryReward> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("mastery").forGetter(MasteryReward::mastery),
            Codec.doubleRange(0, 1e6).fieldOf("amount").forGetter(MasteryReward::amount)
    ).apply(instance, MasteryReward::new));

    @Override
    public RewardType<?> type() {
        return ProgressionModule.MASTERY_REWARD.get();
    }

    @Override
    public Component describe() {
        return Component.translatable(Ids.key("reward", "mastery"), mastery.map(MasteryService::name)
                .orElseGet(() -> Component.translatable(Ids.key("reward", "mastery.grimoire"))), String.format(java.util.Locale.ROOT, "%.0f", amount));
    }

    @Override
    public void grant(ServerPlayer player) {
        mastery.or(() -> MasteryService.elementMastery(player)).ifPresent(id -> MasteryService.add(player, id, amount));
    }
}
