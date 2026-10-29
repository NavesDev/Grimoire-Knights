package com.navesdev.grimoireknights.ability.event;

import com.navesdev.grimoireknights.ability.AbilityModule;
import com.navesdev.grimoireknights.ability.AbilityService;
import com.navesdev.grimoireknights.ability.BurnService;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class AbilityEvents {
    /** Mana regenerates in steps to keep syncs low. */
    private static final int REGEN_INTERVAL = 10;

    private AbilityEvents() {}

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) AbilityService.ensureMana(player);
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % REGEN_INTERVAL == 0) {
            AbilityService.ensureMana(player);
            AbilityService.regenerate(player, AbilityConfig.MANA_REGEN_PER_SECOND.get() * REGEN_INTERVAL / 20.0);
        }
    }

    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living && !living.level().isClientSide && living.hasData(AbilityModule.BURN)) {
            BurnService.tick(living);
        }
    }
}
