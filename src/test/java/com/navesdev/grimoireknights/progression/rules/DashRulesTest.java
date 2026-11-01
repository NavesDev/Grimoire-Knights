package com.navesdev.grimoireknights.progression.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void poseFollowsTheDashRelativeToTheLook() {
        // looking south (+z): the player's right is west (-x)
        assertEquals("dash", DashRules.pose(0, 1, 0, 1));
        assertEquals("dash_back", DashRules.pose(0, -1, 0, 1));
        assertEquals("dash_right", DashRules.pose(-1, 0, 0, 1));
        assertEquals("dash_left", DashRules.pose(1, 0, 0, 1));
        assertEquals("dash", DashRules.pose(-0.6, 0.8, 0, 1));
    }

    @Test
    void rollNeedsAPressJustBeforeLanding() {
        assertTrue(DashRules.rollTimed(90, 100, 20));
        assertTrue(DashRules.rollTimed(80, 100, 20));
        assertFalse(DashRules.rollTimed(79, 100, 20));
        assertFalse(DashRules.rollTimed(-1, 100, 20));
    }

    @Test
    void speedSpreadsTheDistanceOverTheTicks() {
        assertEquals(1.25, DashRules.speedPerTick(5, 4), 1e-9);
    }
}
