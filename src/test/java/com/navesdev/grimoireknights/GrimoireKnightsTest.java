package com.navesdev.grimoireknights;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GrimoireKnightsTest {
    @Test
    void modIdFollowsLoaderRules() {
        assertTrue(GrimoireKnights.MODID.matches("[a-z][a-z0-9_]{1,63}"));
    }
}
