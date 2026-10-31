package com.navesdev.grimoireknights.ability.rules;

/** Fire's own rule: a hit sets the target burning for a fixed total damage over a duration; a new hit refreshes it, never stacks. */
public final class BurnRules {
    private BurnRules() {}

    /** Ticks left after a hit: always the full duration. */
    public static int refresh(int ticksLeft, int durationTicks) {
        return durationTicks;
    }

    public static double damagePerSecond(double totalDamage, int durationTicks) {
        if (durationTicks <= 0 || totalDamage < 0) throw new IllegalArgumentException("invalid burn");
        return totalDamage * 20.0 / durationTicks;
    }

    /** Burn damage still to come, used by Ember burst to deal it at once. */
    public static double remaining(int ticksLeft, double totalDamage, int durationTicks) {
        if (ticksLeft < 0 || durationTicks <= 0) throw new IllegalArgumentException("invalid burn");
        return totalDamage * Math.min(ticksLeft, durationTicks) / durationTicks;
    }
}
