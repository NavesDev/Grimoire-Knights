package com.navesdev.grimoireknights.progression.rules;

/** Mastery curve and gains: progress to the next level is {@code base + mult × level^exp}; weak enemies give almost nothing. */
public final class MasteryRules {
    /** @param maxLevel hard ceiling, above every rank ceiling */
    public record Curve(double base, double mult, double exp, int maxLevel) {}

    public record Progress(int level, double progress, int levelsGained) {}

    private MasteryRules() {}

    public static double required(int level, Curve curve) {
        if (level < 1) throw new IllegalArgumentException("mastery level starts at 1");
        return curve.base() + curve.mult() * Math.pow(level, curve.exp());
    }

    /** Adds progress, levelling up as many times as it reaches; stops at {@code ceiling} (or the curve's max level) with no progress kept. */
    public static Progress add(int level, double progress, double amount, int ceiling, Curve curve) {
        if (level < 1) throw new IllegalArgumentException("mastery level starts at 1");
        if (amount < 0 || progress < 0) throw new IllegalArgumentException("negative mastery progress");
        int cap = Math.min(ceiling, curve.maxLevel());
        if (level >= cap) return new Progress(level, 0, 0);
        int start = level;
        double next = progress + amount;
        while (level < cap && next >= required(level, curve)) {
            next -= required(level, curve);
            level++;
        }
        return new Progress(level, level >= cap ? 0 : next, level - start);
    }

    /** {@code min(1, (enemyValue / (perLevel × level))^exp)}: an enemy worth less than the player's level gives a fraction. */
    public static double strengthFactor(double enemyValue, int level, double perLevel, double exp) {
        if (enemyValue < 0 || level < 1 || perLevel <= 0) throw new IllegalArgumentException("invalid strength factor input");
        return Math.min(1, Math.pow(enemyValue / (perLevel * level), exp));
    }
}
