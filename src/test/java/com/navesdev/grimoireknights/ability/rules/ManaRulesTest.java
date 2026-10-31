package com.navesdev.grimoireknights.ability.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ManaRulesTest {
    @Test
    void maxManaIsTheBasePlusTheTreeBonus() {
        assertEquals(100.0, ManaRules.max(100, 0), 1e-9);
        assertEquals(130.0, ManaRules.max(100, 30), 1e-9);
    }

    @Test
    void regenerateStopsAtMax() {
        assertEquals(55.0, ManaRules.regenerate(50, 100, 5), 1e-9);
        assertEquals(100.0, ManaRules.regenerate(98, 100, 5), 1e-9);
        assertEquals(100.0, ManaRules.regenerate(120, 100, 5), 1e-9);
    }

    @Test
    void manaControlCutsTheCost() {
        assertEquals(20.0, ManaRules.cost(20, 0, 0.05), 1e-9);
        assertEquals(17.0, ManaRules.cost(20, 3, 0.05), 1e-9);
        assertEquals(0.0, ManaRules.cost(20, 30, 0.05), 1e-9);
    }

    @Test
    void rejectsNegativeInput() {
        assertThrows(IllegalArgumentException.class, () -> ManaRules.max(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> ManaRules.regenerate(10, 100, -1));
        assertThrows(IllegalArgumentException.class, () -> ManaRules.cost(10, -1, 0.05));
    }
}
