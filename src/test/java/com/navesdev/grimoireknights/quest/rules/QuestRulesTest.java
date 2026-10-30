package com.navesdev.grimoireknights.quest.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class QuestRulesTest {
    @Test
    void startsANewQuest() {
        assertEquals(QuestRules.StartResult.OK, QuestRules.canStart("a", Set.of(), Set.of(), List.of(), false));
    }

    @Test
    void doesNotStartTwice() {
        assertEquals(QuestRules.StartResult.ALREADY_ACTIVE, QuestRules.canStart("a", Set.of("a"), Set.of(), List.of(), true));
    }

    @Test
    void doneQuestOnlyAgainWhenRepeatable() {
        assertEquals(QuestRules.StartResult.ALREADY_COMPLETED, QuestRules.canStart("a", Set.of(), Set.of("a"), List.of(), false));
        assertEquals(QuestRules.StartResult.OK, QuestRules.canStart("a", Set.of(), Set.of("a"), List.of(), true));
    }

    @Test
    void needsEveryPrerequisiteCompleted() {
        assertEquals(QuestRules.StartResult.MISSING_PREREQUISITE, QuestRules.canStart("b", Set.of(), Set.of(), List.of("a"), false));
        assertEquals(QuestRules.StartResult.MISSING_PREREQUISITE, QuestRules.canStart("c", Set.of(), Set.of("a"), List.of("a", "b"), false));
        assertEquals(QuestRules.StartResult.OK, QuestRules.canStart("c", Set.of(), Set.of("a", "b"), List.of("a", "b"), false));
    }
}
