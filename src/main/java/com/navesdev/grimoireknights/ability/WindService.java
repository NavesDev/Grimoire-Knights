package com.navesdev.grimoireknights.ability;

import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.ability.rules.WindRules;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.vfx.Vfx;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** Wind hits: strength of distance (more damage from afar) and a Tailwind stack for the caster. */
public final class WindService {
    private WindService() {}

    public static double bonus(Entity owner, Entity target) {
        return WindRules.distanceBonus(owner.distanceTo(target), AbilityConfig.WIND_BONUS_START.get(),
                AbilityConfig.WIND_BONUS_PER_BLOCK.get(), AbilityConfig.WIND_BONUS_CAP.get());
    }

    /** Hurts with the distance bonus; true when the hit landed. */
    public static boolean hurt(ServerLevel level, Entity direct, Entity owner, LivingEntity target, double base) {
        double bonus = owner != null ? bonus(owner, target) : 0;
        target.invulnerableTime = 0;
        boolean hit = target.hurt(Spells.source(level, Spells.WIND, direct, owner), (float) (base * (1 + bonus)));
        if (!hit) return false;
        Vfx.play(level, Ids.id("wind_hit"), target.position().add(0, target.getBbHeight() / 2, 0), Map.of("bonus", (float) bonus));
        if (owner instanceof ServerPlayer player) TailwindService.onHit(player);
        return true;
    }
}
