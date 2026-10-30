package com.navesdev.grimoireknights.grimoire;

import net.minecraft.world.level.GameRules;

public final class GrimoireGameRules {
    /** Free choice: at the ceremony each player picks the element (default off, set at world creation). */
    public static final GameRules.Key<GameRules.BooleanValue> FREE_ELEMENT_CHOICE =
            GameRules.register("grimoireknightsFreeElementChoice", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));

    private GrimoireGameRules() {}

    /** Loads the class so the rule registers during mod construction. */
    static void init() {}
}
