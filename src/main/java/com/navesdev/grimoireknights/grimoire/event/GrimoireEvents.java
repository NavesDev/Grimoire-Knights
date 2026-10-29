package com.navesdev.grimoireknights.grimoire.event;

import com.navesdev.grimoireknights.grimoire.GrimoireActivity;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import java.util.Map;
import java.util.WeakHashMap;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Vanilla combat fills the calling and shows the floating grimoire. */
public final class GrimoireEvents {
    private GrimoireEvents() {}

    /** Health before the hit, so an overkill counts only the health actually taken. */
    private static final Map<LivingEntity, Float> HEALTH_BEFORE = new WeakHashMap<>();

    public static void onDamagePre(LivingDamageEvent.Pre event) {
        HEALTH_BEFORE.put(event.getEntity(), event.getEntity().getHealth());
    }

    public static void onDamage(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();
        Float before = HEALTH_BEFORE.remove(victim);
        float damage = Math.min(event.getNewDamage(), before != null ? before : victim.getHealth() + event.getNewDamage());
        if (damage <= 0) return;
        Entity attacker = event.getSource().getEntity();
        // Only hostile mobs count: no fall, fire, drowning, PvP or passive mobs (no attacker, or not an Enemy).
        if (attacker instanceof ServerPlayer player && victim instanceof Enemy) {
            GrimoireService.addCallingDamage(player, damage);
        }
        if (victim instanceof ServerPlayer player && attacker instanceof Enemy) {
            GrimoireService.addCallingDamage(player, damage);
        }
        if (attacker instanceof ServerPlayer player && attacker != victim) {
            GrimoireActivity.combat(player);
        }
        if (victim instanceof ServerPlayer player && attacker instanceof LivingEntity && attacker != victim) {
            GrimoireActivity.combat(player);
        }
    }

    /** A full calling waiting for the ceremony is reminded on login. */
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && !GrimoireService.data(player).ceremonyDone() && GrimoireService.callingFull(player)) {
            GrimoireService.call(player);
        }
    }
}
