package com.navesdev.grimoireknights.progression.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FallRulesTest {
    @Test
    void eachRankCutsItsShare() {
        assertEquals(1f, FallRules.multiplier(0, 0.25), 1e-6);
        assertEquals(0.25f, FallRules.multiplier(3, 0.25), 1e-6);
    }

    @Test
    void neverGoesBelowNoDamage() {
        assertEquals(0f, FallRules.multiplier(5, 0.25));
    }
}
