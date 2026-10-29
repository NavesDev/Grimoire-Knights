package com.navesdev.grimoireknights.ability.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ManaRulesTest {
    @Test
    void maxManaGrowsWithTheStat() {
        assertEquals(100.0, ManaRules.max(0, 100, 10), 1e-9);
        assertEquals(150.0, ManaRules.max(5, 100, 10), 1e-9);
    }

    @Test
    void regenerateStopsAtMax() {
        assertEquals(55.0, ManaRules.regenerate(50, 100, 5), 1e-9);
        assertEquals(100.0, ManaRules.regenerate(98, 100, 5), 1e-9);
        assertEquals(100.0, ManaRules.regenerate(120, 100, 5), 1e-9);
    }

    @Test
    void changingTheStatFillsTheNewMax() {
        assertEquals(150.0, ManaRules.currentAfterStatChange(150), 1e-9);
    }

    @Test
    void rejectsNegativeInput() {
        assertThrows(IllegalArgumentException.class, () -> ManaRules.max(-1, 100, 10));
        assertThrows(IllegalArgumentException.class, () -> ManaRules.regenerate(10, 100, -1));
    }
}
