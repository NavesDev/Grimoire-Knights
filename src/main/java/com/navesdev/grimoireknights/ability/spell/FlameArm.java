package com.navesdev.grimoireknights.ability.spell;

import com.navesdev.grimoireknights.ability.Ability;
import com.navesdev.grimoireknights.ability.Delayed;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.ability.entity.FlameFistEntity;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.progression.SkillTreeService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** Fire, magic 2: a hand of fire grabs, lifts and slams the first target. Upgrades: bigger slam, then a second hand right behind. */
public class FlameArm implements Ability {
    public static final ResourceLocation ID = Ids.id("flame_arm");
    private static final int SECOND_HAND_DELAY = 5;

    @Override
    public double manaCost() {
        return AbilityConfig.FLAME_ARM_COST.get();
    }

    @Override
    public int cooldownTicks() {
        return AbilityConfig.FLAME_ARM_COOLDOWN_TICKS.get();
    }

    @Override
    public ResourceLocation pose() {
        return Ids.id("cast_x_fire");
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int upgrade = SkillTreeService.upgradeLevel(player, ID);
        launch(player, upgrade);
        // the second hand skips the target the first one holds
        if (upgrade >= 2) Delayed.run(player.serverLevel(), SECOND_HAND_DELAY, () -> {
            if (player.isAlive()) launch(player, upgrade);
        });
        return true;
    }

    private static void launch(ServerPlayer player, int upgrade) {
        FlameFistEntity fist = new FlameFistEntity(player.level(), player);
        fist.withUpgrade(upgrade).launch(player, player.getLookAngle(), AbilityConfig.FLAME_ARM_SPEED.get(), AbilityConfig.FLAME_ARM_RANGE.get());
        player.level().addFreshEntity(fist);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8f, 0.7f);
    }
}
