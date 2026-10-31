package com.navesdev.grimoireknights.ability;

import com.navesdev.grimoireknights.ability.attachment.BurnData;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.ability.rules.BurnRules;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.vfx.Vfx;
import java.util.Map;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** Fire's burn: a fire hit sets the target burning for a fixed damage over a few seconds; a new hit refreshes it. */
public final class BurnService {
    private static final int SECOND = 20;

    private BurnService() {}

    public static boolean burning(LivingEntity target) {
        return target.hasData(AbilityModule.BURN) && target.getData(AbilityModule.BURN).ticksLeft() > 0;
    }

    public static void ignite(LivingEntity target, Entity owner) {
        int left = burning(target) ? target.getData(AbilityModule.BURN).ticksLeft() : 0;
        target.setData(AbilityModule.BURN, new BurnData(BurnRules.refresh(left, AbilityConfig.BURN_DURATION_TICKS.get()),
                Optional.ofNullable(owner).map(Entity::getUUID)));
    }

    /** Burn damage still to come; 0 when not burning. */
    public static double remaining(LivingEntity target) {
        if (!burning(target)) return 0;
        return BurnRules.remaining(target.getData(AbilityModule.BURN).ticksLeft(), AbilityConfig.BURN_DAMAGE.get(), AbilityConfig.BURN_DURATION_TICKS.get());
    }

    public static void extinguish(LivingEntity target) {
        target.removeData(AbilityModule.BURN);
    }

    public static void tick(LivingEntity entity) {
        BurnData burn = entity.getData(AbilityModule.BURN);
        if (burn.ticksLeft() <= 0 || !entity.isAlive() || !(entity.level() instanceof ServerLevel level)) {
            entity.removeData(AbilityModule.BURN);
            return;
        }
        // Counted from the last hit, so the first tick of damage lands after the hit's invulnerability.
        int sinceHit = AbilityConfig.BURN_DURATION_TICKS.get() - burn.ticksLeft();
        if (sinceHit > 0 && sinceHit % SECOND == 0) {
            float damage = (float) BurnRules.damagePerSecond(AbilityConfig.BURN_DAMAGE.get(), AbilityConfig.BURN_DURATION_TICKS.get());
            Entity owner = burn.owner().map(level::getEntity).orElse(null);
            if (damage > 0) {
                // ignores the hit cooldown: the burn is its own clock
                entity.invulnerableTime = 0;
                entity.hurt(owner != null ? Spells.source(level, Spells.FIRE, owner, owner) : entity.damageSources().onFire(), damage);
            }
        }
        if (burn.ticksLeft() % SECOND == 0) {
            Vfx.play(level, Ids.id("burn"), entity.position(), Map.of("height", entity.getBbHeight()));
        }
        entity.setData(AbilityModule.BURN, new BurnData(burn.ticksLeft() - 1, burn.owner()));
    }
}
