package com.navesdev.grimoireknights.ability;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.ability.attachment.CombatData;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.ability.rules.CastRules;
import com.navesdev.grimoireknights.ability.rules.ManaRules;
import com.navesdev.grimoireknights.grimoire.Element;
import com.navesdev.grimoireknights.grimoire.GrimoireActivity;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import com.navesdev.grimoireknights.progression.MasteryService;
import com.navesdev.grimoireknights.progression.RankService;
import com.navesdev.grimoireknights.progression.SkillEffects;
import com.navesdev.grimoireknights.progression.SkillTreeService;
import com.navesdev.grimoireknights.progression.config.ProgressionConfig;
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

    /** Base plus the Mana branch's max mana nodes. */
    public static double maxMana(Player player) {
        return ManaRules.max(AbilityConfig.MANA_BASE.get(),
                SkillTreeService.effectRank(player, SkillEffects.MAX_MANA) * ProgressionConfig.MAX_MANA_PER_RANK.get());
    }

    /** Base plus the Mana branch's regeneration nodes. */
    public static double regenPerSecond(Player player) {
        return AbilityConfig.MANA_REGEN_PER_SECOND.get() + SkillTreeService.effectRank(player, SkillEffects.MANA_REGEN) * ProgressionConfig.REGEN_PER_RANK.get();
    }

    /** Mana cost after Mana Control; spells and broom flight both use it. */
    public static double cost(Player player, double base) {
        return ManaRules.cost(base, SkillTreeService.effectRank(player, SkillEffects.MANA_CONTROL), ProgressionConfig.MANA_CONTROL_DISCOUNT.get());
    }

    /** Spends mana and trains the Mana mastery with it. False, spending nothing, when there is not enough. */
    public static boolean spend(ServerPlayer player, double amount) {
        CombatData data = data(player);
        if (amount <= 0) return true;
        if (data.mana() < amount) return false;
        player.setData(AbilityModule.COMBAT_DATA, data.withMana(data.mana() - amount));
        MasteryService.add(player, MasteryService.resourceMastery(player), amount * ProgressionConfig.MANA_SPENT_RATIO.get());
        return true;
    }

    /** New players start with full mana. */
    public static void ensureMana(ServerPlayer player) {
        CombatData data = data(player);
        if (!data.manaSet()) player.setData(AbilityModule.COMBAT_DATA, data.withMana(maxMana(player)));
    }

    public static void setMana(ServerPlayer player, double value) {
        player.setData(AbilityModule.COMBAT_DATA, data(player).withMana(Math.min(value, maxMana(player))));
    }

    public static void regenerate(ServerPlayer player, double amount) {
        CombatData data = data(player);
        double max = maxMana(player);
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
        double cost = cost(player, ability.map(Ability::manaCost).orElse(0.0));
        CastRules.Result result = CastRules.check(player.isAlive() && !player.isSpectator(), element.isPresent(), ability.isPresent(),
                RankService.slotUnlocked(player, slot), data.mana(), id.map(i -> data.cooldownLeft(i, now)).orElse(0L), cost);
        if (result != CastRules.Result.OK) {
            GrimoireKnights.LOGGER.debug("Cast denied for {} slot {}: {}", player.getGameProfile().getName(), slot, result);
            return result;
        }
        if (!ability.get().cast(player)) {
            GrimoireKnights.LOGGER.debug("Cast of {} failed for {}", id.get(), player.getGameProfile().getName());
            return CastRules.Result.FAILED;
        }
        spend(player, cost);
        player.setData(AbilityModule.COMBAT_DATA, data(player).withCooldown(id.get(), now + ability.get().cooldownTicks()));
        GrimoireActivity.cast(player);
        return result;
    }
}
