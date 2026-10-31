package com.navesdev.grimoireknights.ability.spell;

import com.navesdev.grimoireknights.ability.Ability;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.ability.entity.WindBowEntity;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.progression.SkillTreeService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Wind, magic 3: a bow of wind shoots large arrows in sequence. Upgrades: arrows split in 3 on hit, then intercept projectiles. */
public class GaleBow implements Ability {
    public static final ResourceLocation ID = Ids.id("gale_bow");

    @Override
    public double manaCost() {
        return AbilityConfig.BOW_COST.get();
    }

    @Override
    public int cooldownTicks() {
        return AbilityConfig.BOW_COOLDOWN_TICKS.get();
    }

    @Override
    public ResourceLocation pose() {
        return Ids.id("cast_c_wind");
    }

    @Override
    public boolean cast(ServerPlayer player) {
        player.level().addFreshEntity(new WindBowEntity(player.level(), player, SkillTreeService.upgradeLevel(player, ID)));
        return true;
    }
}
