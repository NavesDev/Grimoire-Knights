package com.navesdev.grimoireknights.quest.rules;

import java.util.Collection;
import java.util.Set;

/** Whether a player may start a quest. */
public final class QuestRules {
    public enum StartResult { OK, ALREADY_ACTIVE, ALREADY_COMPLETED, MISSING_PREREQUISITE }

    private QuestRules() {}

    public static <K> StartResult canStart(K quest, Set<K> active, Set<K> completed, Collection<K> requires, boolean repeatable) {
        if (active.contains(quest)) return StartResult.ALREADY_ACTIVE;
        if (completed.contains(quest) && !repeatable) return StartResult.ALREADY_COMPLETED;
        if (!completed.containsAll(requires)) return StartResult.MISSING_PREREQUISITE;
        return StartResult.OK;
    }
}
