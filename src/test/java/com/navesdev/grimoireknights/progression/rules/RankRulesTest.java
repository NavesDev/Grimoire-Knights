package com.navesdev.grimoireknights.progression.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RankRulesTest {
    private static final List<Integer> CEILINGS = List.of(10, 20, 30, 40, 50);

    @Test
    void eachRankHasItsCeiling() {
        assertEquals(10, RankRules.ceiling(Rank.RECRUIT, CEILINGS));
        assertEquals(40, RankRules.ceiling(Rank.CAPTAIN, CEILINGS));
        assertEquals(50, RankRules.ceiling(Rank.MAGIC_KING, CEILINGS));
    }

    @Test
    void slotsUnlockWithTheRank() {
        assertTrue(RankRules.slotUnlocked(Rank.RECRUIT, 0));
        assertFalse(RankRules.slotUnlocked(Rank.RECRUIT, 1));
        assertTrue(RankRules.slotUnlocked(Rank.KNIGHT, 1));
        assertFalse(RankRules.slotUnlocked(Rank.KNIGHT, 2));
        assertTrue(RankRules.slotUnlocked(Rank.ELITE_KNIGHT, 2));
        assertFalse(RankRules.slotUnlocked(Rank.MAGIC_KING, 3));
        assertFalse(RankRules.slotUnlocked(Rank.MAGIC_KING, -1));
    }

    @Test
    void promotionGoesOneStepAndStopsAtTheTop() {
        assertEquals(Optional.of(Rank.KNIGHT), Rank.RECRUIT.next());
        assertEquals(Optional.empty(), Rank.MAGIC_KING.next());
    }

    @Test
    void ranksReadBackFromTheirIds() {
        for (Rank rank : Rank.values()) assertEquals(Optional.of(rank), Rank.byId(rank.id()));
        assertEquals(Optional.empty(), Rank.byId("emperor"));
    }
}
