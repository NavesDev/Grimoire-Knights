package com.navesdev.grimoireknights.progression;

import com.navesdev.grimoireknights.progression.attachment.RankData;
import com.navesdev.grimoireknights.progression.config.ProgressionConfig;
import com.navesdev.grimoireknights.progression.rules.Rank;
import com.navesdev.grimoireknights.progression.rules.RankRules;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** The player's rank: grimoire mastery ceiling and unlocked spell slots. */
public final class RankService {
    private RankService() {}

    public static Rank rank(Player player) {
        return player.getData(ProgressionModule.RANK_DATA).rank();
    }

    public static void set(ServerPlayer player, Rank rank) {
        player.setData(ProgressionModule.RANK_DATA, new RankData(rank));
    }

    /** One step up; empty at the top. */
    public static Optional<Rank> promote(ServerPlayer player) {
        Optional<Rank> next = rank(player).next();
        next.ifPresent(rank -> set(player, rank));
        return next;
    }

    public static int ceiling(Player player) {
        return RankRules.ceiling(rank(player), List.copyOf(ProgressionConfig.RANK_CEILINGS.get()));
    }

    public static boolean slotUnlocked(Player player, int slot) {
        return RankRules.slotUnlocked(rank(player), slot);
    }
}
