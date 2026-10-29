package com.navesdev.grimoireknights.ability.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class BurnRulesTest {
    @Test
    void eachHitAddsAStackUpToTheCap() {
        assertEquals(1, BurnRules.addStack(0, 5));
        assertEquals(5, BurnRules.addStack(4, 5));
        assertEquals(5, BurnRules.addStack(5, 5));
    }

    @Test
    void moreStacksMoreDamagePerSecond() {
        assertEquals(0.0, BurnRules.damagePerSecond(0, 0.5), 1e-9);
        assertEquals(1.5, BurnRules.damagePerSecond(3, 0.5), 1e-9);
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> BurnRules.addStack(0, 0));
        assertThrows(IllegalArgumentException.class, () -> BurnRules.damagePerSecond(-1, 0.5));
    }
}
