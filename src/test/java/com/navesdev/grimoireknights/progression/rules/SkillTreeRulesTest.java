package com.navesdev.grimoireknights.progression.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SkillTreeRulesTest {
    private static final SkillTreeRules.Node<String> CONTROL = new SkillTreeRules.Node<>(List.of(1, 2, 3), List.of(), Rank.RECRUIT);
    private static final SkillTreeRules.Node<String> PASSIVE = new SkillTreeRules.Node<>(List.of(4), List.of("z1", "x1"), Rank.KNIGHT);

    @Test
    void buysWhenEverythingIsMet() {
        assertEquals(SkillTreeRules.Result.OK, SkillTreeRules.canBuy(CONTROL, 0, 1, Map.of(), Rank.RECRUIT));
        assertEquals(SkillTreeRules.Result.OK, SkillTreeRules.canBuy(PASSIVE, 0, 4, Map.of("z1", 1, "x1", 1), Rank.KNIGHT));
    }

    @Test
    void showsOnlyWhatIsBoughtOrNextInLine() {
        assertTrue(SkillTreeRules.revealed(CONTROL, 0, Map.of()));
        assertFalse(SkillTreeRules.revealed(PASSIVE, 0, Map.of("z1", 1)));
        assertTrue(SkillTreeRules.revealed(PASSIVE, 0, Map.of("z1", 1, "x1", 1)));
        assertTrue(SkillTreeRules.revealed(PASSIVE, 1, Map.of()));
    }

    @Test
    void theNextRankCostsItsOwnPrice() {
        assertEquals(2, SkillTreeRules.nextCost(CONTROL, 1));
        assertEquals(SkillTreeRules.Result.NO_POINTS, SkillTreeRules.canBuy(CONTROL, 1, 1, Map.of(), Rank.RECRUIT));
        assertEquals(SkillTreeRules.Result.OK, SkillTreeRules.canBuy(CONTROL, 2, 3, Map.of(), Rank.RECRUIT));
    }

    @Test
    void deniesWhenMaxedOrMissingSomething() {
        assertEquals(SkillTreeRules.Result.MAXED, SkillTreeRules.canBuy(CONTROL, 3, 99, Map.of(), Rank.RECRUIT));
        assertEquals(SkillTreeRules.Result.MISSING_PREREQUISITE, SkillTreeRules.canBuy(PASSIVE, 0, 4, Map.of("z1", 1), Rank.KNIGHT));
        assertEquals(SkillTreeRules.Result.MISSING_PREREQUISITE, SkillTreeRules.canBuy(PASSIVE, 0, 4, Map.of("z1", 1, "x1", 0), Rank.KNIGHT));
        assertEquals(SkillTreeRules.Result.RANK_TOO_LOW, SkillTreeRules.canBuy(PASSIVE, 0, 4, Map.of("z1", 1, "x1", 1), Rank.RECRUIT));
    }

    @Test
    void refundReturnsEveryPointSpent() {
        assertEquals(0, SkillTreeRules.refund(CONTROL, 0));
        assertEquals(3, SkillTreeRules.refund(CONTROL, 2));
        assertEquals(6, SkillTreeRules.refund(CONTROL, 3));
    }
}
