package com.navesdev.grimoireknights.ability;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.ability.attachment.CombatData;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.ability.rules.CastRules;
import com.navesdev.grimoireknights.ability.rules.ManaRules;
import com.navesdev.grimoireknights.grimoire.Element;
import com.navesdev.grimoireknights.grimoire.GrimoireActivity;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Mana, cooldowns and casting, on the server. */
public final class AbilityService {
    private AbilityService() {}

    public static CombatData data(Player player) {
        return player.getData(AbilityModule.COMBAT_DATA);
    }

    public static double maxMana(int manaStat) {
        return ManaRules.max(manaStat, AbilityConfig.MANA_BASE.get(), AbilityConfig.MANA_PER_POINT.get());
    }

    public static double maxMana(Player player) {
        return maxMana(data(player).manaStat());
    }

    /** New players start with full mana. */
    public static void ensureMana(ServerPlayer player) {
        CombatData data = data(player);
        if (!data.manaSet()) player.setData(AbilityModule.COMBAT_DATA, data.withMana(maxMana(data.manaStat())));
    }

    /** Changing the Mana stat changes both maximum and current mana. */
    public static void setManaStat(ServerPlayer player, int stat) {
        player.setData(AbilityModule.COMBAT_DATA, data(player).withManaStat(stat, ManaRules.currentAfterStatChange(maxMana(stat))));
    }

    public static void setMana(ServerPlayer player, double value) {
        player.setData(AbilityModule.COMBAT_DATA, data(player).withMana(Math.min(value, maxMana(player))));
    }

    public static void regenerate(ServerPlayer player, double amount) {
        CombatData data = data(player);
        double max = maxMana(data.manaStat());
        if (!data.manaSet() || data.mana() >= max) return;
        player.setData(AbilityModule.COMBAT_DATA, data.withMana(ManaRules.regenerate(data.mana(), max, amount)));
    }

    public static void resetCooldowns(ServerPlayer player) {
        player.setData(AbilityModule.COMBAT_DATA, data(player).withoutCooldowns());
    }

    /** Validates grimoire, slot, cooldown and mana, then casts. Anything off denies. */
    public static CastRules.Result tryCast(ServerPlayer player, int slot) {
        ensureMana(player);
        Optional<Element> element = GrimoireService.element(player);
        Optional<ResourceLocation> id = element.flatMap(e -> e.ability(slot));
        Optional<Ability> ability = id.flatMap(AbilityRegistries.ABILITIES::getOptional);
        CombatData data = data(player);
        long now = player.serverLevel().getGameTime();
        CastRules.Result result = CastRules.check(player.isAlive() && !player.isSpectator(), element.isPresent(), ability.isPresent(), data.mana(),
                id.map(i -> data.cooldownLeft(i, now)).orElse(0L), ability.map(Ability::manaCost).orElse(0.0));
        if (result != CastRules.Result.OK) {
            GrimoireKnights.LOGGER.debug("Cast denied for {} slot {}: {}", player.getGameProfile().getName(), slot, result);
            return result;
        }
        if (!ability.get().cast(player)) {
            GrimoireKnights.LOGGER.debug("Cast of {} failed for {}", id.get(), player.getGameProfile().getName());
            return CastRules.Result.FAILED;
        }
        player.setData(AbilityModule.COMBAT_DATA, data
                .withMana(data.mana() - ability.get().manaCost())
                .withCooldown(id.get(), now + ability.get().cooldownTicks()));
        GrimoireActivity.cast(player);
        return result;
    }
}
