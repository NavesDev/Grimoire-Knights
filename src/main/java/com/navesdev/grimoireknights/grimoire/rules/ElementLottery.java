package com.navesdev.grimoireknights.grimoire.rules;

import java.util.Map;
import java.util.function.IntUnaryOperator;

/** Weighted draw: each entry's chance is its weight over the sum of all weights. */
public final class ElementLottery {
    private ElementLottery() {}

    /** @param random returns a uniform int in [0, bound) */
    public static <K> K draw(Map<K, Integer> weights, IntUnaryOperator random) {
        return pick(weights, random.applyAsInt(total(weights)));
    }

    /** Entry whose cumulative range contains {@code roll}, in the map's iteration order. */
    public static <K> K pick(Map<K, Integer> weights, int roll) {
        int total = total(weights);
        if (roll < 0 || roll >= total) {
            throw new IllegalArgumentException("roll " + roll + " outside [0, " + total + ")");
        }
        for (Map.Entry<K, Integer> entry : weights.entrySet()) {
            roll -= entry.getValue();
            if (roll < 0) return entry.getKey();
        }
        throw new IllegalStateException("unreachable");
    }

    private static int total(Map<?, Integer> weights) {
        int total = 0;
        for (int weight : weights.values()) {
            if (weight < 0) throw new IllegalArgumentException("negative weight " + weight);
            total += weight;
        }
        if (total <= 0) throw new IllegalArgumentException("weights sum to " + total);
        return total;
    }
}
