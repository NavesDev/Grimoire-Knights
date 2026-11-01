package com.navesdev.grimoireknights.progression;

import com.navesdev.grimoireknights.progression.config.ProgressionConfig;
import com.navesdev.grimoireknights.progression.event.ProgressionEvents;
import com.navesdev.grimoireknights.progression.rules.DamageRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/** Every hit grows with the attacker's combat level (players: masteries; mobs: data map), after armor; spells then lose the victim's resistance. */
public final class DamageScaling {
    private DamageScaling() {}

    /** Element mastery for a player's spell, Body for the rest; a mob's data map entry, 1 when unlisted. */
    public static int level(LivingEntity attacker, DamageSource source) {
        if (attacker instanceof Player player) {
            if (!ProgressionEvents.isSpell(source)) return MasteryService.get(player, Masteries.BODY).level();
            return MasteryService.elementMastery(player).map(element -> MasteryService.get(player, element).level()).orElse(1);
        }
        CombatLevel level = attacker.getType().builtInRegistryHolder().getData(ProgressionRegistries.COMBAT_LEVEL);
        return level != null ? Math.min(level.level(), ProgressionConfig.MAX_LEVEL.get()) : 1;
    }

    /** 1 with no living attacker or when hurting oneself. */
    public static double multiplier(DamageSource source, LivingEntity victim) {
        if (!(source.getEntity() instanceof LivingEntity attacker) || attacker == victim) return 1;
        return DamageRules.multiplier(level(attacker, source), ProgressionConfig.DAMAGE_PER_LEVEL.get());
    }

    public static void onDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();
        double damage = event.getNewDamage() * multiplier(event.getSource(), victim);
        if (ProgressionEvents.isSpell(event.getSource()) && victim.getAttributes().hasAttribute(ProgressionModule.SPELL_RESISTANCE)) {
            damage = DamageRules.afterResistance(damage, victim.getAttributeValue(ProgressionModule.SPELL_RESISTANCE));
        }
        event.setNewDamage((float) damage);
    }
}
