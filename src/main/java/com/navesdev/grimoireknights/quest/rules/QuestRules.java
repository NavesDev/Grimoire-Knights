package com.navesdev.grimoireknights.quest.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/** Whether a player may start a quest, and how objective progress adds up. */
public final class QuestRules {
    public enum StartResult { OK, ALREADY_ACTIVE, ALREADY_COMPLETED, MISSING_PREREQUISITE }

    private QuestRules() {}

    public static <K> StartResult canStart(K quest, Set<K> active, Set<K> completed, Collection<K> requires, boolean repeatable) {
        if (active.contains(quest)) return StartResult.ALREADY_ACTIVE;
        if (completed.contains(quest) && !repeatable) return StartResult.ALREADY_COMPLETED;
        if (!completed.containsAll(requires)) return StartResult.MISSING_PREREQUISITE;
        return StartResult.OK;
    }

    /** Adds each gain to its objective, capped at what it requires. */
    public static List<Integer> advance(List<Integer> progress, List<Integer> required, List<Integer> gains) {
        if (progress.size() != required.size() || gains.size() != required.size()) throw new IllegalArgumentException("one entry per objective");
        List<Integer> next = new ArrayList<>(progress.size());
        for (int i = 0; i < progress.size(); i++) next.add(Math.min(required.get(i), progress.get(i) + Math.max(0, gains.get(i))));
        return next;
    }

    public static boolean done(List<Integer> progress, List<Integer> required) {
        if (progress.size() != required.size()) return false;
        for (int i = 0; i < progress.size(); i++) if (progress.get(i) < required.get(i)) return false;
        return true;
    }
}
