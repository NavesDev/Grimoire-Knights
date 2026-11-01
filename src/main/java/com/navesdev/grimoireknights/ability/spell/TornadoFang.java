package com.navesdev.grimoireknights.ability.spell;

import com.navesdev.grimoireknights.ability.Ability;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.ability.entity.TornadoEntity;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.progression.SkillTreeService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** Wind, magic 2: a horizontal tornado thrown where the caster looks; it drills through and pushes every enemy in its way. */
public class TornadoFang implements Ability {
    public static final ResourceLocation ID = Ids.id("tornado_fang");

    @Override
    public double manaCost() {
        return AbilityConfig.TORNADO_COST.get();
    }

    @Override
    public int cooldownTicks() {
        return AbilityConfig.TORNADO_COOLDOWN_TICKS.get();
    }

    @Override
    public ResourceLocation pose() {
        return Ids.id("cast_x_wind");
    }

    @Override
    public boolean cast(ServerPlayer player) {
        TornadoEntity tornado = new TornadoEntity(player.level(), player);
        tornado.withUpgrade(SkillTreeService.upgradeLevel(player, ID));
        tornado.launch(player, player.getLookAngle(), AbilityConfig.TORNADO_SPEED.get(), AbilityConfig.TORNADO_RANGE.get());
        // centered on the launch point: the base (what hits blocks) clears the ground when thrown level
        tornado.setPos(tornado.getX(), tornado.getY() - tornado.getBbHeight() / 2, tornado.getZ());
        player.level().addFreshEntity(tornado);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BREEZE_IDLE_AIR, SoundSource.PLAYERS, 1, 0.8f);
        return true;
    }
}
