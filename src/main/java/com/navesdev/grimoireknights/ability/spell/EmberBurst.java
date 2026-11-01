package com.navesdev.grimoireknights.ability.spell;

import com.navesdev.grimoireknights.ability.Ability;
import com.navesdev.grimoireknights.ability.BurnService;
import com.navesdev.grimoireknights.ability.Spells;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.vfx.Vfx;
import com.navesdev.grimoireknights.progression.SkillTreeService;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Fire, magic 3: a burst that hits every nearby enemy, needing no other spell first. Burning enemies are detonated: they
 * also take the burn left at once, and it goes out. Upgrades: longer range, then each detonated enemy spreads burn
 * around it. It goes off (mana, cooldown) even with no enemy in range, so it can't be spammed for a sure hit.
 */
public class EmberBurst implements Ability {
    public static final ResourceLocation ID = Ids.id("ember_burst");

    @Override
    public double manaCost() {
        return AbilityConfig.EMBER_COST.get();
    }

    @Override
    public int cooldownTicks() {
        return AbilityConfig.EMBER_COOLDOWN_TICKS.get();
    }

    @Override
    public ResourceLocation pose() {
        return Ids.id("cast_c_fire");
    }

    @Override
    public boolean cast(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        int upgrade = SkillTreeService.upgradeLevel(player, ID);
        double range = upgrade >= 1 ? AbilityConfig.EMBER_BIG_RANGE.get() : AbilityConfig.EMBER_RANGE.get();
        List<LivingEntity> targets = Spells.around(level, player.position(), range, player, target -> true);
        Vfx.play(level, Ids.id("ember_burst_caster"), player.position(), Map.of("radius", (float) range));
        for (LivingEntity target : targets) {
            boolean burning = BurnService.burning(target);
            double damage = AbilityConfig.EMBER_DAMAGE.get() + (burning ? BurnService.remaining(target) : 0);
            if (burning) BurnService.extinguish(target);
            target.invulnerableTime = 0;
            target.hurt(Spells.source(level, Spells.FIRE, player, player), (float) damage);
            Vfx.play(level, Ids.id("ember_burst_target"), target.position(), Map.of());
            if (burning && upgrade >= 2) {
                Spells.around(level, target.position(), AbilityConfig.EMBER_SPREAD_RADIUS.get(), player, other -> other != target)
                        .forEach(other -> BurnService.ignite(other, player));
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1, 0.8f);
        return true;
    }
}
