package com.navesdev.grimoireknights.progression.rules;

/** Combat level and spell resistance: every hit is {@code base × (1 + perLevel × (level − 1))}, after armor; spells then lose the resistance share. */
public final class DamageRules {
    /** Highest share of spell damage resistance can cut. */
    public static final double MAX_RESISTANCE = 0.8;

    private DamageRules() {}

    public static double multiplier(int level, double perLevel) {
        if (level < 1 || perLevel < 0) throw new IllegalArgumentException("invalid combat level input");
        return 1 + perLevel * (level - 1);
    }

    public static double afterResistance(double damage, double resistance) {
        if (damage < 0) throw new IllegalArgumentException("negative damage");
        return damage * (1 - Math.max(0, Math.min(MAX_RESISTANCE, resistance)));
    }
}
