package com.navesdev.grimoireknights.ability.rules;

/** Mana: the base plus the skill tree bonus sets the maximum; abilities spend it and it regenerates once spending stops for a while. */
public final class ManaRules {
    private ManaRules() {}

    public static double max(double base, double treeBonus) {
        if (base < 0 || treeBonus < 0) throw new IllegalArgumentException("negative mana input");
        return base + treeBonus;
    }

    public static double regenerate(double current, double max, double amount) {
        if (amount < 0) throw new IllegalArgumentException("negative regeneration");
        return Math.min(max, current + amount);
    }

    /** Regeneration resumes only {@code delayTicks} after the last mana spent, so a steady stream of casts runs dry. */
    public static boolean regenerates(long now, long lastSpent, int delayTicks) {
        return now - delayTicks >= lastSpent;
    }

    /** Mana Control: each rank cuts the cost by {@code discountPerRank}, never below zero. */
    public static double cost(double base, int controlRank, double discountPerRank) {
        if (base < 0 || controlRank < 0 || discountPerRank < 0) throw new IllegalArgumentException("negative cost input");
        return base * Math.max(0, 1 - controlRank * discountPerRank);
    }
}
