package com.navesdev.grimoireknights.progression.rules;

import java.util.Arrays;
import java.util.Optional;

/** Knight ranks, lowest first. */
public enum Rank {
    RECRUIT("recruit"),
    KNIGHT("knight"),
    ELITE_KNIGHT("elite_knight"),
    CAPTAIN("captain"),
    MAGIC_KING("magic_king");

    private final String id;

    Rank(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public Optional<Rank> next() {
        return ordinal() + 1 < values().length ? Optional.of(values()[ordinal() + 1]) : Optional.empty();
    }

    public boolean atLeast(Rank other) {
        return compareTo(other) >= 0;
    }

    public static Optional<Rank> byId(String id) {
        return Arrays.stream(values()).filter(rank -> rank.id.equals(id)).findFirst();
    }
}
