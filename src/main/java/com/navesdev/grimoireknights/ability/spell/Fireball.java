package com.navesdev.grimoireknights.ability.spell;

import com.navesdev.grimoireknights.ability.Ability;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.ability.entity.FireballProjectile;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.progression.SkillTreeService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** Fire, magic 1: a fireball that flies straight, hurts and burns. Upgrades: bigger, then ground fire. */
public class Fireball implements Ability {
    public static final ResourceLocation ID = Ids.id("fireball");

    @Override
    public double manaCost() {
        return AbilityConfig.FIREBALL_COST.get();
    }

    @Override
    public int cooldownTicks() {
        return AbilityConfig.FIREBALL_COOLDOWN_TICKS.get();
    }

    @Override
    public ResourceLocation pose() {
        return Ids.id("cast_z");
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int upgrade = SkillTreeService.upgradeLevel(player, ID);
        FireballProjectile fireball = new FireballProjectile(player.level(), player);
        fireball.withUpgrade(upgrade).withScale(upgrade >= 1 ? AbilityConfig.FIREBALL_BIG_SCALE.get().floatValue() : 1f);
        fireball.setPos(player.getEyePosition().add(player.getLookAngle().scale(0.8)).subtract(0, 0.2, 0));
        fireball.shoot(player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z, AbilityConfig.FIREBALL_SPEED.get().floatValue(), 0);
        player.level().addFreshEntity(fireball);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8f, 1.2f);
        return true;
    }
}
