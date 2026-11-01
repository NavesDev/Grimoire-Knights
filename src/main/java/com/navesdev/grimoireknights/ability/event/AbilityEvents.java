package com.navesdev.grimoireknights.ability.event;

import com.navesdev.grimoireknights.ability.AbilityModule;
import com.navesdev.grimoireknights.ability.AbilityService;
import com.navesdev.grimoireknights.ability.BurnService;
import com.navesdev.grimoireknights.ability.Delayed;
import com.navesdev.grimoireknights.ability.HeldService;
import com.navesdev.grimoireknights.ability.PhoenixService;
import com.navesdev.grimoireknights.ability.TailwindService;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
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
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        TailwindService.tick(player);
        if (player.tickCount % REGEN_INTERVAL == 0) {
            AbilityService.ensureMana(player);
            AbilityService.regenerate(player, AbilityService.regenPerSecond(player) * REGEN_INTERVAL / 20.0);
        }
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) Delayed.tick(level);
    }

    /** Phoenix: a fire player with the passive revives instead of dying. */
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && PhoenixService.tryRevive(player)) event.setCanceled(true);
    }

    /** A held target can't attack. */
    public static void onIncoming(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker && attacker != event.getEntity() && HeldService.held(attacker)) {
            event.setCanceled(true);
        }
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TailwindService.forget(player);
            AbilityService.forget(player);
            PhoenixService.forget(player);
            HeldService.release(player);
        }
    }

    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living && !living.level().isClientSide && living.hasData(AbilityModule.BURN)) {
            BurnService.tick(living);
        }
    }
}
