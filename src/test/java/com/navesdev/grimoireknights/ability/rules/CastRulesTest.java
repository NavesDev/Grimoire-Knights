package com.navesdev.grimoireknights.ability.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CastRulesTest {
    @Test
    void castsWhenEverythingIsReady() {
        assertEquals(CastRules.Result.OK, CastRules.check(true, true, 100, 0, 10));
    }

    @Test
    void deniesWithoutGrimoire() {
        assertEquals(CastRules.Result.NO_GRIMOIRE, CastRules.check(false, true, 100, 0, 10));
    }

    @Test
    void deniesAnEmptySlot() {
        assertEquals(CastRules.Result.NO_ABILITY, CastRules.check(true, false, 100, 0, 10));
    }

    @Test
    void deniesWhileCoolingDown() {
        assertEquals(CastRules.Result.ON_COOLDOWN, CastRules.check(true, true, 100, 5, 10));
    }

    @Test
    void deniesWithoutEnoughMana() {
        assertEquals(CastRules.Result.NO_MANA, CastRules.check(true, true, 9.99, 0, 10));
        assertEquals(CastRules.Result.OK, CastRules.check(true, true, 10, 0, 10));
    }
}
