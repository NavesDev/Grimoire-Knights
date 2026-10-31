package com.navesdev.grimoireknights.progression.quest;

import com.mojang.serialization.MapCodec;
import com.navesdev.grimoireknights.progression.ProgressionModule;
import com.navesdev.grimoireknights.progression.RankService;
import com.navesdev.grimoireknights.progression.attachment.RankData;
import com.navesdev.grimoireknights.progression.rules.Rank;
import com.navesdev.grimoireknights.quest.Reward;
import com.navesdev.grimoireknights.quest.RewardType;
import com.navesdev.grimoireknights.core.Ids;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** {@code rank_up}: promotes to {@code rank} (never down), or one step up when it is missing. */
public record RankUpReward(Optional<Rank> rank) implements Reward {
    public static final MapCodec<RankUpReward> CODEC = RankData.RANK_CODEC.optionalFieldOf("rank").xmap(RankUpReward::new, RankUpReward::rank);

    @Override
    public RewardType<?> type() {
        return ProgressionModule.RANK_UP.get();
    }

    @Override
    public Component describe() {
        return rank.map(r -> Component.translatable(Ids.key("reward", "rank"), RankService.name(r)))
                .orElseGet(() -> Component.translatable(Ids.key("reward", "rank_next")));
    }

    @Override
    public void grant(ServerPlayer player) {
        if (rank.isEmpty()) RankService.promote(player);
        else if (!RankService.rank(player).atLeast(rank.get())) RankService.set(player, rank.get());
    }
}
