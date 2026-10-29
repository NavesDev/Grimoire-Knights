package com.navesdev.grimoireknights.ability.rules;

/** Mana: the Mana stat sets the maximum; current mana is spent by abilities and regenerates over time. */
public final class ManaRules {
    private ManaRules() {}

    public static double max(int manaStat, double base, double perPoint) {
        if (manaStat < 0 || base < 0 || perPoint < 0) throw new IllegalArgumentException("negative mana input");
        return base + manaStat * perPoint;
    }

    public static double regenerate(double current, double max, double amount) {
        if (amount < 0) throw new IllegalArgumentException("negative regeneration");
        return Math.min(max, current + amount);
    }

    /** Changing the Mana stat changes both (design: "subir o status aumenta as duas"): current mana becomes the new maximum. */
    public static double currentAfterStatChange(double newMax) {
        return newMax;
    }
}
