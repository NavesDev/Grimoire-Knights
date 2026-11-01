package com.navesdev.grimoireknights.ability.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class WindRulesTest {
    private static double bonus(double distance) {
        return WindRules.distanceBonus(distance, 6, 0.03, 0.6);
    }

    @Test
    void normalDamageUpToSixBlocks() {
        assertEquals(0.0, bonus(0), 1e-9);
        assertEquals(0.0, bonus(6), 1e-9);
    }

    @Test
    void threePercentPerBlockAfterThat() {
        assertEquals(0.03, bonus(7), 1e-9);
        assertEquals(0.30, bonus(16), 1e-9);
    }

    @Test
    void cappedAtSixtyPercentFromTwentySixBlocks() {
        assertEquals(0.6, bonus(26), 1e-9);
        assertEquals(0.6, bonus(80), 1e-9);
    }

    @Test
    void tailwindStacksUpToTheCapAndCutCooldowns() {
        assertEquals(1, WindRules.addStack(0, 5));
        assertEquals(5, WindRules.addStack(5, 5));
        assertEquals(100, WindRules.cooldown(100, 0, 0.06));
        assertEquals(70, WindRules.cooldown(100, 5, 0.06));
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> bonus(-1));
        assertThrows(IllegalArgumentException.class, () -> WindRules.addStack(0, 0));
    }
}
