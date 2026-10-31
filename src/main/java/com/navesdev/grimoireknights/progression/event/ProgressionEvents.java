package com.navesdev.grimoireknights.progression.event;

import com.navesdev.grimoireknights.progression.CombatTracker;
import com.navesdev.grimoireknights.progression.DashService;
import com.navesdev.grimoireknights.progression.Masteries;
import com.navesdev.grimoireknights.progression.MasteryService;
import com.navesdev.grimoireknights.progression.ProgressionRegistries;
import com.navesdev.grimoireknights.progression.config.ProgressionConfig;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Combat trains the masteries: spells the element, physical hits and damage taken the Body. */
public final class ProgressionEvents {
    /** Health before the hit, so an overkill counts only the health actually taken. */
    private static final Map<LivingEntity, Float> HEALTH_BEFORE = new WeakHashMap<>();

    private ProgressionEvents() {}

    public static boolean isSpell(DamageSource source) {
        return source.is(ProgressionRegistries.SPELL);
    }

    public static void onIncoming(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && DashService.invulnerable(player)) event.setCanceled(true);
    }

    public static void onDamagePre(LivingDamageEvent.Pre event) {
        HEALTH_BEFORE.put(event.getEntity(), event.getEntity().getHealth());
    }

    public static void onDamage(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();
        Float before = HEALTH_BEFORE.remove(victim);
        float damage = Math.min(event.getNewDamage(), before != null ? before : victim.getHealth() + event.getNewDamage());
        if (damage <= 0 || !(event.getSource().getEntity() instanceof LivingEntity attacker) || attacker == victim) return;
        if (attacker instanceof ServerPlayer player) {
            CombatTracker.hit(player);
            if (isSpell(event.getSource())) {
                MasteryService.elementMastery(player).ifPresent(element ->
                        MasteryService.addFromDamage(player, element, victim, damage, ProgressionConfig.SPELL_DAMAGE_RATIO.get()));
            } else {
                MasteryService.addFromDamage(player, Masteries.BODY, victim, damage, ProgressionConfig.PHYSICAL_DAMAGE_RATIO.get());
            }
        }
        if (victim instanceof ServerPlayer player) {
            CombatTracker.hit(player);
            MasteryService.addFromDamage(player, Masteries.BODY, attacker, damage, ProgressionConfig.DAMAGE_TAKEN_RATIO.get());
        }
    }

    /** A spell kill grants the enemy's mastery value to the element. */
    public static void onDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player && player != event.getEntity() && isSpell(event.getSource())) {
            MasteryService.elementMastery(player).ifPresent(element -> MasteryService.addFromKill(player, element, event.getEntity()));
        }
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) DashService.tick(player);
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DashService.forget(player);
            CombatTracker.forget(player);
        }
    }
}
