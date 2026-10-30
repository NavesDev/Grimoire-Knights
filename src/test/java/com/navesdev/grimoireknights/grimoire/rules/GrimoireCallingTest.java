package com.navesdev.grimoireknights.grimoire.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GrimoireCallingTest {
    @Test
    void onePointPerHeart() {
        assertEquals(1.0, GrimoireCalling.points(2.0f, 2.0), 1e-9);
        assertEquals(3.5, GrimoireCalling.points(7.0f, 2.0), 1e-9);
    }

    @Test
    void noPointsForNoDamage() {
        assertEquals(0.0, GrimoireCalling.points(0f, 2.0), 1e-9);
        assertEquals(0.0, GrimoireCalling.points(-3f, 2.0), 1e-9);
    }

    @Test
    void addStopsAtTheTop() {
        assertEquals(60.0, GrimoireCalling.add(50.0, 10.0, 100.0), 1e-9);
        assertEquals(100.0, GrimoireCalling.add(95.0, 10.0, 100.0), 1e-9);
        assertEquals(100.0, GrimoireCalling.add(100.0, 10.0, 100.0), 1e-9);
    }

    @Test
    void fullOnlyAtTheTop() {
        assertFalse(GrimoireCalling.isFull(99.99, 100.0));
        assertTrue(GrimoireCalling.isFull(100.0, 100.0));
    }

    @Test
    void justFilledOnlyWhenCrossing() {
        assertTrue(GrimoireCalling.justFilled(99.0, 100.0, 100.0));
        assertFalse(GrimoireCalling.justFilled(100.0, 100.0, 100.0));
        assertFalse(GrimoireCalling.justFilled(50.0, 60.0, 100.0));
    }

    @Test
    void rejectsInvalidConfig() {
        assertThrows(IllegalArgumentException.class, () -> GrimoireCalling.points(2f, 0));
        assertThrows(IllegalArgumentException.class, () -> GrimoireCalling.add(0, 1, 0));
    }
}
