package com.navesdev.grimoireknights.ability.spell;

import com.navesdev.grimoireknights.ability.Ability;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.ability.entity.FireballProjectile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/** Fire, magic 1: a fireball that flies straight, hurts and stacks burn. */
public class Fireball implements Ability {
    @Override
    public double manaCost() {
        return AbilityConfig.FIREBALL_COST.get();
    }

    @Override
    public int cooldownTicks() {
        return AbilityConfig.FIREBALL_COOLDOWN_TICKS.get();
    }

    @Override
    public boolean cast(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        FireballProjectile fireball = new FireballProjectile(player.level(), player);
        fireball.setPos(player.getEyePosition().add(look.scale(0.8)).subtract(0, 0.2, 0));
        fireball.shoot(look.x, look.y, look.z, AbilityConfig.FIREBALL_SPEED.get().floatValue(), 0);
        player.level().addFreshEntity(fireball);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8f, 1.2f);
        return true;
    }
}
