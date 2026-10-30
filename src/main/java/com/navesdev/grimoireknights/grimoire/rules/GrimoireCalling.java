package com.navesdev.grimoireknights.grimoire.rules;

/** The grimoire's calling: a hidden bar filled by vanilla combat that unlocks the ceremony when full. */
public final class GrimoireCalling {
    private GrimoireCalling() {}

    /** Points earned for {@code damage} health dealt to or taken from a hostile mob. */
    public static double points(float damage, double healthPerPoint) {
        if (healthPerPoint <= 0) throw new IllegalArgumentException("healthPerPoint must be positive");
        return damage > 0 ? damage / healthPerPoint : 0;
    }

    public static double add(double current, double points, double max) {
        if (max <= 0) throw new IllegalArgumentException("max must be positive");
        return Math.min(max, current + points);
    }

    public static boolean isFull(double current, double max) {
        return current >= max;
    }

    public static boolean justFilled(double before, double after, double max) {
        return !isFull(before, max) && isFull(after, max);
    }
}
