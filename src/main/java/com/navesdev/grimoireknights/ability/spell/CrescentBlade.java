package com.navesdev.grimoireknights.ability.spell;

import com.navesdev.grimoireknights.ability.Ability;
import com.navesdev.grimoireknights.ability.Delayed;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.ability.entity.WindBladeEntity;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.progression.SkillTreeService;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/** Wind, magic 1: a fast sickle through a few enemies. Upgrades: a fan of 3 blades, then a line volley behind the fan with longer range. */
public class CrescentBlade implements Ability {
    public static final ResourceLocation ID = Ids.id("crescent_blade");
    private static final int FAN_DEGREES = 15;
    private static final int VOLLEY_BLADES = 3;
    private static final int VOLLEY_DELAY = 3;

    @Override
    public double manaCost() {
        return AbilityConfig.BLADE_COST.get();
    }

    @Override
    public int cooldownTicks() {
        return AbilityConfig.BLADE_COOLDOWN_TICKS.get();
    }

    @Override
    public ResourceLocation pose() {
        return Ids.id("cast_z");
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int upgrade = SkillTreeService.upgradeLevel(player, ID);
        double range = upgrade >= 2 ? AbilityConfig.BLADE_VOLLEY_RANGE.get() : AbilityConfig.BLADE_RANGE.get();
        Vec3 look = player.getLookAngle();
        Set<UUID> fan = new HashSet<>();
        blade(player, look, range, fan);
        if (upgrade >= 1) {
            blade(player, look.yRot((float) Math.toRadians(FAN_DEGREES)), range, fan);
            blade(player, look.yRot((float) Math.toRadians(-FAN_DEGREES)), range, fan);
        }
        if (upgrade >= 2) {
            for (int i = 1; i <= VOLLEY_BLADES; i++) {
                Delayed.run(player.serverLevel(), i * VOLLEY_DELAY, () -> {
                    if (player.isAlive()) blade(player, player.getLookAngle(), range, new HashSet<>());
                });
            }
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.6f, 1.6f);
        return true;
    }

    private static void blade(ServerPlayer player, Vec3 direction, double range, Set<UUID> wave) {
        WindBladeEntity blade = new WindBladeEntity(player.level(), player).sharing(wave);
        blade.launch(player, direction, AbilityConfig.BLADE_SPEED.get(), range);
        player.level().addFreshEntity(blade);
    }
}
