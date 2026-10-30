package com.navesdev.grimoireknights.ability;

import net.minecraft.server.level.ServerPlayer;

/** A magic or sword in a slot (Z, X, C). One class per ability, registered in {@code grimoireknights:ability}. */
public interface Ability {
    double manaCost();

    int cooldownTicks();

    /** Runs on the server after the checks passed; false when it could not happen (nothing is spent). */
    boolean cast(ServerPlayer player);
}
