package com.navesdev.grimoireknights.ability.rules;

/** Fire's own rule: burn stacks up, more hits mean more damage per second. */
public final class BurnRules {
    private BurnRules() {}

    public static int addStack(int stacks, int maxStacks) {
        if (maxStacks <= 0) throw new IllegalArgumentException("maxStacks must be positive");
        return Math.min(maxStacks, stacks + 1);
    }

    public static double damagePerSecond(int stacks, double damagePerStack) {
        if (stacks < 0 || damagePerStack < 0) throw new IllegalArgumentException("negative burn input");
        return stacks * damagePerStack;
    }
}
