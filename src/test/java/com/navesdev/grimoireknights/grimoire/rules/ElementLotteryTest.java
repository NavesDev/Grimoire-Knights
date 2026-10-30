package com.navesdev.grimoireknights.grimoire.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ElementLotteryTest {
    private static Map<String, Integer> weights(Object... pairs) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((String) pairs[i], (Integer) pairs[i + 1]);
        }
        return map;
    }

    @Test
    void rollPicksTheEntryWhoseRangeContainsIt() {
        Map<String, Integer> w = weights("fire", 100, "wind", 100, "none", 20);
        assertEquals("fire", ElementLottery.pick(w, 0));
        assertEquals("fire", ElementLottery.pick(w, 99));
        assertEquals("wind", ElementLottery.pick(w, 100));
        assertEquals("none", ElementLottery.pick(w, 200));
        assertEquals("none", ElementLottery.pick(w, 219));
    }

    @Test
    void zeroWeightIsNeverPicked() {
        Map<String, Integer> w = weights("fire", 0, "wind", 5);
        for (int roll = 0; roll < 5; roll++) {
            assertEquals("wind", ElementLottery.pick(w, roll));
        }
    }

    @Test
    void chancesFollowTheWeights() {
        Map<String, Integer> w = weights("fire", 100, "none", 20);
        Random random = new Random(42);
        int none = 0;
        int draws = 120_000;
        for (int i = 0; i < draws; i++) {
            if (ElementLottery.draw(w, random::nextInt).equals("none")) none++;
        }
        assertEquals(20.0 / 120, (double) none / draws, 0.005);
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> ElementLottery.draw(weights(), bound -> 0));
        assertThrows(IllegalArgumentException.class, () -> ElementLottery.draw(weights("fire", 0), bound -> 0));
        assertThrows(IllegalArgumentException.class, () -> ElementLottery.draw(weights("fire", -1, "wind", 5), bound -> 0));
        assertThrows(IllegalArgumentException.class, () -> ElementLottery.pick(weights("fire", 5), 5));
    }
}
