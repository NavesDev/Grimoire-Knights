package com.navesdev.grimoireknights.progression.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DamageRulesTest {
    @Test
    void levelOneKeepsTheBaseDamage() {
        assertEquals(1.0, DamageRules.multiplier(1, 0.04), 1e-9);
    }

    @Test
    void eachLevelAddsItsShareOfTheBase() {
        assertEquals(1.36, DamageRules.multiplier(10, 0.04), 1e-9);
        assertEquals(2.16, DamageRules.multiplier(30, 0.04), 1e-9);
        assertEquals(2.96, DamageRules.multiplier(50, 0.04), 1e-9);
    }

    @Test
    void spellResistanceCutsItsShare() {
        assertEquals(6.8, DamageRules.afterResistance(10, 0.32), 1e-9);
        assertEquals(10.0, DamageRules.afterResistance(10, 0), 1e-9);
    }

    @Test
    void resistanceIsClampedToItsCeiling() {
        assertEquals(2.0, DamageRules.afterResistance(10, 3), 1e-9);
    }

    @Test
    void invalidInputFails() {
        assertThrows(IllegalArgumentException.class, () -> DamageRules.multiplier(0, 0.04));
        assertThrows(IllegalArgumentException.class, () -> DamageRules.multiplier(5, -0.1));
        assertThrows(IllegalArgumentException.class, () -> DamageRules.afterResistance(-1, 0));
    }
}
