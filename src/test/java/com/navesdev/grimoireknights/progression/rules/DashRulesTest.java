package com.navesdev.grimoireknights.progression.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DashRulesTest {
    @Test
    void dashesTowardTheMovement() {
        double[] dir = DashRules.direction(3, 4, 1, 0);
        assertEquals(0.6, dir[0], 1e-9);
        assertEquals(0.8, dir[1], 1e-9);
    }

    @Test
    void standingStillDashesTowardTheLook() {
        double[] dir = DashRules.direction(0, 0, 0, -2);
        assertEquals(0.0, dir[0], 1e-9);
        assertEquals(-1.0, dir[1], 1e-9);
    }

    @Test
    void noDirectionAtAllGivesNoDash() {
        double[] dir = DashRules.direction(0, 0, 0, 0);
        assertEquals(0.0, dir[0], 1e-9);
        assertEquals(0.0, dir[1], 1e-9);
    }

    @Test
    void speedSpreadsTheDistanceOverTheTicks() {
        assertEquals(1.25, DashRules.speedPerTick(5, 4), 1e-9);
    }
}
