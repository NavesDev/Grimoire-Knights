package com.navesdev.grimoireknights.progression.rules;

import java.util.List;

/** The rank sets the grimoire mastery ceiling and unlocks the spell slots: X at Knight, C at Elite Knight. */
public final class RankRules {
    private static final List<Rank> SLOT_RANKS = List.of(Rank.RECRUIT, Rank.KNIGHT, Rank.ELITE_KNIGHT);

    private RankRules() {}

    /** @param ceilings one per rank, in rank order */
    public static int ceiling(Rank rank, List<Integer> ceilings) {
        if (ceilings.size() != Rank.values().length) throw new IllegalArgumentException("one ceiling per rank");
        return ceilings.get(rank.ordinal());
    }

    public static boolean slotUnlocked(Rank rank, int slot) {
        return slot >= 0 && slot < SLOT_RANKS.size() && rank.atLeast(SLOT_RANKS.get(slot));
    }

    public static Rank slotRank(int slot) {
        return SLOT_RANKS.get(slot);
    }
}
