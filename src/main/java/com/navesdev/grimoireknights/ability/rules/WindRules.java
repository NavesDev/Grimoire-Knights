package com.navesdev.grimoireknights.ability.rules;

/** Wind's own rule (strength of distance) and the Tailwind passive's stacks. */
public final class WindRules {
    private WindRules() {}

    /** Extra damage fraction: 0 up to {@code start} blocks, then {@code perBlock} per block, up to {@code cap}. */
    public static double distanceBonus(double distance, double start, double perBlock, double cap) {
        if (distance < 0) throw new IllegalArgumentException("negative distance");
        return Math.min(cap, Math.max(0, distance - start) * perBlock);
    }

    public static int addStack(int stacks, int maxStacks) {
        if (maxStacks <= 0) throw new IllegalArgumentException("maxStacks must be positive");
        return Math.min(maxStacks, stacks + 1);
    }

    /** Wind spell cooldown shortened by each Tailwind stack. */
    public static int cooldown(int ticks, int stacks, double cutPerStack) {
        return (int) Math.round(ticks * Math.max(0, 1 - stacks * cutPerStack));
    }
}
