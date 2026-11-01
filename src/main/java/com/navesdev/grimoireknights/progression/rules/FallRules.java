package com.navesdev.grimoireknights.progression.rules;

/** Fall damage left after the Fall Resistance ranks. */
public final class FallRules {
    private FallRules() {}

    /** Fall damage multiplier: 1 with no rank, 0 once the ranks add up to the whole damage. */
    public static float multiplier(int rank, double reductionPerRank) {
        return (float) Math.max(0, 1 - rank * reductionPerRank);
    }
}
