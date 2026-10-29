package com.navesdev.grimoireknights.ability.rules;

/** Server-side check before an ability is cast; any doubt denies. */
public final class CastRules {
    public enum Result { OK, NO_GRIMOIRE, NO_ABILITY, ON_COOLDOWN, NO_MANA }

    private CastRules() {}

    public static Result check(boolean hasGrimoire, boolean slotHasAbility, double mana, long cooldownTicksLeft, double cost) {
        if (!hasGrimoire) return Result.NO_GRIMOIRE;
        if (!slotHasAbility) return Result.NO_ABILITY;
        if (cooldownTicksLeft > 0) return Result.ON_COOLDOWN;
        if (mana < cost) return Result.NO_MANA;
        return Result.OK;
    }
}
