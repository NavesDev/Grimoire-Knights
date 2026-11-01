package com.navesdev.grimoireknights.progression.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MasteryRulesTest {
    private static final MasteryRules.Curve CURVE = new MasteryRules.Curve(20, 5, 1.5, 50);

    @Test
    void requiredFollowsTheCurve() {
        assertEquals(25.0, MasteryRules.required(1, CURVE), 1e-9);
        assertEquals(20 + 5 * Math.pow(4, 1.5), MasteryRules.required(4, CURVE), 1e-9);
    }

    @Test
    void progressBelowTheNextLevelStays() {
        MasteryRules.Progress result = MasteryRules.add(1, 0, 10, 50, CURVE);
        assertEquals(new MasteryRules.Progress(1, 10, 0), result);
    }

    @Test
    void enoughProgressLevelsUpAndKeepsTheRest() {
        MasteryRules.Progress result = MasteryRules.add(1, 20, 10, 50, CURVE);
        assertEquals(2, result.level());
        assertEquals(5.0, result.progress(), 1e-9);
        assertEquals(1, result.levelsGained());
    }

    @Test
    void aBigGainGrantsSeveralLevels() {
        MasteryRules.Progress result = MasteryRules.add(1, 0, 500, 50, CURVE);
        assertEquals(result.level() - 1, result.levelsGained());
        assertEquals(true, result.levelsGained() > 3);
    }

    @Test
    void progressStopsAtTheCeiling() {
        MasteryRules.Progress result = MasteryRules.add(9, 0, 10_000, 10, CURVE);
        assertEquals(new MasteryRules.Progress(10, 0, 1), result);
        assertEquals(new MasteryRules.Progress(10, 0, 0), MasteryRules.add(10, 0, 50, 10, CURVE));
    }

    @Test
    void theMaxLevelIsAHardCeiling() {
        assertEquals(50, MasteryRules.add(49, 0, 1e9, 99, CURVE).level());
    }

    @Test
    void weakEnemiesGiveAlmostNothing() {
        assertEquals(1.0, MasteryRules.strengthFactor(10, 1, 2, 2), 1e-9);
        assertEquals(0.0625, MasteryRules.strengthFactor(10, 20, 2, 2), 1e-9);
        assertEquals(1.0, MasteryRules.strengthFactor(300, 20, 2, 2), 1e-9);
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> MasteryRules.add(1, 0, -1, 50, CURVE));
        assertThrows(IllegalArgumentException.class, () -> MasteryRules.add(0, 0, 1, 50, CURVE));
        assertThrows(IllegalArgumentException.class, () -> MasteryRules.strengthFactor(-1, 1, 2, 2));
    }
}
