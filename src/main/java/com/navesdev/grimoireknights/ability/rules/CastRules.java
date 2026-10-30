package com.navesdev.grimoireknights.ability.rules;

/** Server-side check before an ability is cast; any doubt denies. */
public final class CastRules {
    public enum Result { OK, CANNOT_ACT, NO_GRIMOIRE, NO_ABILITY, ON_COOLDOWN, NO_MANA, FAILED }

    private CastRules() {}

    /** @param canAct false for spectators and the dead */
    public static Result check(boolean canAct, boolean hasGrimoire, boolean slotHasAbility, double mana, long cooldownTicksLeft, double cost) {
        if (!canAct) return Result.CANNOT_ACT;
        if (!hasGrimoire) return Result.NO_GRIMOIRE;
        if (!slotHasAbility) return Result.NO_ABILITY;
        if (cooldownTicksLeft > 0) return Result.ON_COOLDOWN;
        if (mana < cost) return Result.NO_MANA;
        return Result.OK;
    }
}
