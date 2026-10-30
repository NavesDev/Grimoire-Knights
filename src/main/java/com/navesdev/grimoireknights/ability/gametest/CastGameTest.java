package com.navesdev.grimoireknights.ability.gametest;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.ability.AbilityService;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.ability.rules.CastRules;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.gametest.TestPlayers;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GrimoireKnights.MODID)
@PrefixGameTestTemplate(false)
public class CastGameTest {
    @GameTest(template = "empty")
    public static void castNeedsAGrimoire(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        helper.assertTrue(AbilityService.tryCast(player, 0) == CastRules.Result.NO_GRIMOIRE, "no grimoire, no magic");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void castSpendsManaAndStartsTheCooldown(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        GrimoireService.give(player, Ids.id("fire"), GrimoireService.CEREMONY_LEAVES);
        AbilityService.ensureMana(player);
        double before = AbilityService.data(player).mana();
        helper.assertTrue(AbilityService.tryCast(player, 0) == CastRules.Result.OK, "first cast");
        double spent = before - AbilityService.data(player).mana();
        helper.assertTrue(Math.abs(spent - AbilityConfig.FIREBALL_COST.get()) < 1e-6, "fireball cost spent, got " + spent);
        helper.assertTrue(AbilityService.tryCast(player, 0) == CastRules.Result.ON_COOLDOWN, "second cast waits for the cooldown");
        helper.assertTrue(AbilityService.tryCast(player, 1) == CastRules.Result.NO_ABILITY, "empty slot denied");
        AbilityService.resetCooldowns(player);
        AbilityService.setMana(player, 0);
        helper.assertTrue(AbilityService.tryCast(player, 0) == CastRules.Result.NO_MANA, "no mana denied");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void manaRegeneratesUpToTheMax(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        AbilityService.ensureMana(player);
        double max = AbilityService.maxMana(player);
        AbilityService.setMana(player, max - 1);
        AbilityService.regenerate(player, 5);
        helper.assertTrue(AbilityService.data(player).mana() == max, "regeneration stops at the max");
        AbilityService.setManaStat(player, 5);
        helper.assertTrue(AbilityService.data(player).mana() == AbilityService.maxMana(player) && AbilityService.maxMana(player) > max,
                "raising the Mana stat raises max and current");
        helper.succeed();
    }
}
