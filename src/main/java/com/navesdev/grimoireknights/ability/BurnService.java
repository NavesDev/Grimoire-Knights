package com.navesdev.grimoireknights.ability;

import com.navesdev.grimoireknights.ability.attachment.BurnData;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.ability.rules.BurnRules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/** Fire's burn: each hit adds a stack; every second the target takes damage for its stacks. */
public final class BurnService {
    private static final int SECOND = 20;

    private BurnService() {}

    public static void addStack(LivingEntity target) {
        int stacks = target.hasData(AbilityModule.BURN) ? target.getData(AbilityModule.BURN).stacks() : 0;
        target.setData(AbilityModule.BURN, new BurnData(BurnRules.addStack(stacks, AbilityConfig.BURN_MAX_STACKS.get()),
                AbilityConfig.BURN_DURATION_TICKS.get()));
    }

    public static void tick(LivingEntity entity) {
        BurnData burn = entity.getData(AbilityModule.BURN);
        if (burn.ticksLeft() <= 0 || !entity.isAlive()) {
            entity.removeData(AbilityModule.BURN);
            return;
        }
        // Counted from the last hit, so the first tick of damage lands after the hit's invulnerability.
        int sinceHit = AbilityConfig.BURN_DURATION_TICKS.get() - burn.ticksLeft();
        if (sinceHit > 0 && sinceHit % SECOND == 0) {
            float damage = (float) BurnRules.damagePerSecond(burn.stacks(), AbilityConfig.BURN_DAMAGE_PER_STACK.get());
            if (damage > 0) entity.hurt(entity.damageSources().onFire(), damage);
        }
        // Flames only for show: vanilla fire ticks would add damage of their own.
        if (entity.level() instanceof ServerLevel level && burn.ticksLeft() % 4 == 0) {
            level.sendParticles(ParticleTypes.FLAME, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                    burn.stacks(), entity.getBbWidth() * 0.4, entity.getBbHeight() * 0.3, entity.getBbWidth() * 0.4, 0.01);
        }
        entity.setData(AbilityModule.BURN, new BurnData(burn.stacks(), burn.ticksLeft() - 1));
    }
}
