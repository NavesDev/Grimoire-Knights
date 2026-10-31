package com.navesdev.grimoireknights.ability.gametest;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.ability.AbilityModule;
import com.navesdev.grimoireknights.ability.AbilityService;
import com.navesdev.grimoireknights.ability.BurnService;
import com.navesdev.grimoireknights.ability.HeldService;
import com.navesdev.grimoireknights.ability.PhoenixService;
import com.navesdev.grimoireknights.ability.WindService;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.ability.rules.CastRules;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.gametest.TestPlayers;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import com.navesdev.grimoireknights.progression.RankService;
import com.navesdev.grimoireknights.progression.SkillTreeService;
import com.navesdev.grimoireknights.progression.rules.Rank;
import java.util.Optional;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GrimoireKnights.MODID)
@PrefixGameTestTemplate(false)
public class SpellGameTest {
    @GameTest(template = "empty")
    public static void burnRefreshesInsteadOfStacking(GameTestHelper helper) {
        ServerPlayer caster = TestPlayers.create(helper);
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(1.5, 1, 1.5));
        BurnService.ignite(pig, caster);
        double full = BurnService.remaining(pig);
        for (int i = 0; i < 30; i++) BurnService.tick(pig);
        helper.assertTrue(BurnService.remaining(pig) < full, "the burn wears down");
        BurnService.ignite(pig, caster);
        BurnService.ignite(pig, caster);
        helper.assertTrue(Math.abs(BurnService.remaining(pig) - AbilityConfig.BURN_DAMAGE.get()) < 1e-6, "a new hit resets to the full burn, never more");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theBurnDealsItsWholeDamage(GameTestHelper helper) {
        ServerPlayer caster = TestPlayers.create(helper);
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(1.5, 1, 1.5));
        float health = pig.getHealth();
        BurnService.ignite(pig, caster);
        for (int i = 0; i <= AbilityConfig.BURN_DURATION_TICKS.get() + 1; i++) {
            if (pig.hasData(AbilityModule.BURN)) BurnService.tick(pig);
        }
        helper.assertTrue(Math.abs(health - pig.getHealth() - AbilityConfig.BURN_DAMAGE.get()) < 1e-3, "full burn dealt, got " + (health - pig.getHealth()));
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aHeldPlayerCannotDash(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        SkillTreeService.setPoints(player, com.navesdev.grimoireknights.progression.Masteries.BODY, 2);
        SkillTreeService.buy(player, Ids.id("dash"));
        HeldService.hold(player, helper.getLevel().getGameTime() + 40);
        helper.assertTrue(com.navesdev.grimoireknights.progression.DashService.tryDash(player, 1, 0) == com.navesdev.grimoireknights.progression.DashService.Result.CANNOT_ACT, "held: no dash");
        HeldService.release(player);
        com.navesdev.grimoireknights.progression.DashService.forget(player);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void emberBurstDetonatesTheBurnAndPutsItOut(GameTestHelper helper) {
        ServerPlayer caster = TestPlayers.create(helper);
        GrimoireService.give(caster, Ids.id("fire"), GrimoireService.CEREMONY_LEAVES);
        RankService.set(caster, Rank.ELITE_KNIGHT);
        AbilityService.ensureMana(caster);
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(1.5, 1, 3.5));
        caster.setPos(helper.absoluteVec(new Vec3(1.5, 1, 1.5)));
        helper.assertTrue(AbilityService.tryCast(caster, 2) == CastRules.Result.FAILED, "nothing burning: fails and costs nothing");
        BurnService.ignite(pig, caster);
        float health = pig.getHealth();
        helper.assertTrue(AbilityService.tryCast(caster, 2) == CastRules.Result.OK, "ember burst");
        float expected = (float) (AbilityConfig.BURN_DAMAGE.get() + AbilityConfig.EMBER_BONUS.get());
        helper.assertTrue(Math.abs(health - pig.getHealth() - expected) < 1e-3, "remaining burn + bonus, got " + (health - pig.getHealth()));
        helper.assertFalse(BurnService.burning(pig), "the burn is out");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void windHitsHarderFromAfar(GameTestHelper helper) {
        ServerPlayer caster = TestPlayers.create(helper);
        Pig near = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(1.5, 1, 4.5));
        Pig far = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(1.5, 1, 17.5));
        caster.setPos(helper.absoluteVec(new Vec3(1.5, 1, 1.5)));
        helper.assertTrue(WindService.bonus(caster, near) == 0, "normal damage up to 6 blocks");
        helper.assertTrue(Math.abs(WindService.bonus(caster, far) - 0.03 * (16 - 6)) < 1e-6, "+3% per block after 6, got " + WindService.bonus(caster, far));
        float health = far.getHealth();
        WindService.hurt(helper.getLevel(), caster, caster, far, 5);
        helper.assertTrue(Math.abs(health - far.getHealth() - 5 * 1.3f) < 1e-3, "bonus applied to the damage");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aHeldTargetCannotAttackOrCast(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        GrimoireService.give(player, Ids.id("wind"), GrimoireService.CEREMONY_LEAVES);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(1.5, 1, 1.5));
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(2.5, 1, 1.5));
        HeldService.hold(zombie, helper.getLevel().getGameTime() + 40);
        float health = pig.getHealth();
        pig.hurt(helper.getLevel().damageSources().mobAttack(zombie), 3);
        helper.assertTrue(pig.getHealth() == health, "held attacker deals nothing");
        HeldService.hold(player, helper.getLevel().getGameTime() + 40);
        helper.assertTrue(AbilityService.tryCast(player, 0) == CastRules.Result.CANNOT_ACT, "held caster can't cast");
        HeldService.release(player);
        helper.assertTrue(AbilityService.tryCast(player, 0) == CastRules.Result.OK, "released caster casts the crescent blade");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lockedSlotsAndBothElementsInTheLottery(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        GrimoireService.give(player, Ids.id("fire"), GrimoireService.CEREMONY_LEAVES);
        helper.assertTrue(AbilityService.tryCast(player, 1) == CastRules.Result.LOCKED, "X locked for a recruit");
        helper.assertTrue(AbilityService.tryCast(player, 2) == CastRules.Result.LOCKED, "C locked for a recruit");
        RankService.set(player, Rank.KNIGHT);
        helper.assertTrue(AbilityService.tryCast(player, 1) == CastRules.Result.OK, "X at Knight");
        helper.assertTrue(GrimoireService.lotteryWeights().containsKey(Optional.of(Ids.id("wind"))), "wind is in the lottery");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void phoenixRevivesOncePerFight(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        GrimoireService.give(player, Ids.id("fire"), GrimoireService.CEREMONY_LEAVES);
        helper.assertFalse(PhoenixService.tryRevive(player), "no passive, no revive");
        RankService.set(player, Rank.ELITE_KNIGHT);
        SkillTreeService.setPoints(player, Ids.id("fire"), 13);
        for (String node : new String[] {"fire/fireball_1", "fire/flame_arm_1", "fire/ember_burst_1", "fire/phoenix"}) SkillTreeService.buy(player, Ids.id(node));
        helper.assertTrue(SkillTreeService.hasPassive(player), "phoenix bought");
        helper.assertTrue(PhoenixService.tryRevive(player), "revives");
        helper.assertTrue(player.getHealth() == player.getMaxHealth() * AbilityConfig.PHOENIX_HEALTH.get().floatValue(), "half health");
        helper.assertFalse(PhoenixService.tryRevive(player), "only once per fight");
        PhoenixService.forget(player);
        helper.succeed();
    }
}
