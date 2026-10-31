package com.navesdev.grimoireknights.ability.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class BurnRulesTest {
    @Test
    void aBurnDealsItsTotalOverItsDuration() {
        assertEquals(1.0, BurnRules.damagePerSecond(4, 80), 1e-9);
        assertEquals(2.0, BurnRules.damagePerSecond(4, 40), 1e-9);
    }

    @Test
    void aNewHitRefreshesInsteadOfStacking() {
        assertEquals(80, BurnRules.refresh(10, 80));
        assertEquals(80, BurnRules.refresh(80, 80));
        assertEquals(80, BurnRules.refresh(0, 80));
    }

    @Test
    void remainingDamageShrinksWithTheTimeLeft() {
        assertEquals(4.0, BurnRules.remaining(80, 4, 80), 1e-9);
        assertEquals(1.0, BurnRules.remaining(20, 4, 80), 1e-9);
        assertEquals(0.0, BurnRules.remaining(0, 4, 80), 1e-9);
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> BurnRules.damagePerSecond(4, 0));
        assertThrows(IllegalArgumentException.class, () -> BurnRules.remaining(-1, 4, 80));
    }
}
