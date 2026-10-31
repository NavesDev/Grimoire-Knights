package com.navesdev.grimoireknights.progression.gametest;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.gametest.TestPlayers;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import com.navesdev.grimoireknights.progression.DashService;
import com.navesdev.grimoireknights.progression.Masteries;
import com.navesdev.grimoireknights.progression.MasteryService;
import com.navesdev.grimoireknights.progression.RankService;
import com.navesdev.grimoireknights.progression.SkillTreeService;
import com.navesdev.grimoireknights.progression.quest.RankUpReward;
import com.navesdev.grimoireknights.progression.rules.Rank;
import com.navesdev.grimoireknights.progression.rules.SkillTreeRules;
import java.util.Optional;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GrimoireKnights.MODID)
@PrefixGameTestTemplate(false)
public class ProgressionGameTest {
    private static final ResourceLocation FIRE = Ids.id("fire");

    @GameTest(template = "empty")
    public static void levelsGivePointsToTheirBranch(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        MasteryService.add(player, Masteries.BODY, 1_000);
        int level = MasteryService.get(player, Masteries.BODY).level();
        helper.assertTrue(level > 1, "a big gain levels up, got " + level);
        helper.assertTrue(SkillTreeService.data(player).points(Masteries.BODY) == level - 1, "one point per level gained");
        helper.assertTrue(SkillTreeService.data(player).points(Masteries.MANA) == 0, "points stay in their branch");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void grimoireMasteryIsPerElementAndStopsAtTheRankCeiling(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        GrimoireService.give(player, FIRE, GrimoireService.CEREMONY_LEAVES);
        MasteryService.add(player, FIRE, 1e9);
        helper.assertTrue(MasteryService.get(player, FIRE).level() == RankService.ceiling(player), "recruit stops at the ceiling");
        MasteryService.add(player, FIRE, 1e9);
        helper.assertTrue(MasteryService.get(player, FIRE).level() == RankService.ceiling(player), "still stopped");
        new RankUpReward(Optional.empty()).grant(player);
        helper.assertTrue(RankService.rank(player) == Rank.KNIGHT, "rank_up promotes one step");
        MasteryService.add(player, FIRE, 1e9);
        helper.assertTrue(MasteryService.get(player, FIRE).level() == RankService.ceiling(player) && RankService.ceiling(player) > 10, "resumes after the promotion");
        helper.assertTrue(MasteryService.get(player, Ids.id("wind")).level() == 1, "another element keeps its own mastery");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void weakEnemiesGiveAlmostNothing(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        LivingEntity zombie = helper.spawn(EntityType.ZOMBIE, 1, 2, 1);
        double fresh = MasteryService.strengthFactor(player, Masteries.BODY, zombie);
        MasteryService.set(player, Masteries.BODY, 30);
        double strong = MasteryService.strengthFactor(player, Masteries.BODY, zombie);
        helper.assertTrue(fresh == 1 && strong < 0.05, "factor " + fresh + " then " + strong);
        LivingEntity wither = helper.spawn(EntityType.WITHER, 1, 2, 1);
        helper.assertTrue(MasteryService.enemyValue(wither) == 300 && MasteryService.enemyValue(zombie) == 10, "data map and default values");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void nodesNeedPointsPrerequisitesAndRank(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        GrimoireService.give(player, FIRE, GrimoireService.CEREMONY_LEAVES);
        helper.assertTrue(SkillTreeService.buy(player, Ids.id("dash")) == SkillTreeRules.Result.NO_POINTS, "no points");
        SkillTreeService.setPoints(player, Masteries.BODY, 2);
        helper.assertTrue(SkillTreeService.buy(player, Ids.id("dash")) == SkillTreeRules.Result.OK, "dash bought");
        helper.assertTrue(SkillTreeService.buy(player, Ids.id("dash")) == SkillTreeRules.Result.MAXED, "dash has one rank");
        SkillTreeService.setPoints(player, FIRE, 20);
        helper.assertTrue(SkillTreeService.buy(player, Ids.id("fire/fireball_2")) == SkillTreeRules.Result.MISSING_PREREQUISITE, "upgrades in sequence");
        helper.assertTrue(SkillTreeService.buy(player, Ids.id("fire/flame_arm_1")) == SkillTreeRules.Result.RANK_TOO_LOW, "X upgrade needs Knight");
        helper.assertTrue(SkillTreeService.buy(player, Ids.id("wind/crescent_blade_1")) == SkillTreeRules.Result.WRONG_ELEMENT, "other element's node");
        helper.assertTrue(SkillTreeService.buy(player, Ids.id("fire/fireball_1")) == SkillTreeRules.Result.OK, "first upgrade");
        helper.assertTrue(SkillTreeService.upgradeLevel(player, Ids.id("fireball")) == 1, "upgrade level read by ability");
        SkillTreeService.reset(player);
        helper.assertTrue(SkillTreeService.data(player).points(Masteries.BODY) == 2 && SkillTreeService.data(player).points(FIRE) == 20, "reset refunds");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void slotsUnlockWithTheRank(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        GrimoireService.give(player, FIRE, GrimoireService.CEREMONY_LEAVES);
        helper.assertTrue(!RankService.slotUnlocked(player, 1), "X locked for a recruit");
        RankService.set(player, Rank.ELITE_KNIGHT);
        helper.assertTrue(RankService.slotUnlocked(player, 1) && RankService.slotUnlocked(player, 2), "Elite Knight unlocks X and C");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dashNeedsItsNodeAndWaitsTheCooldown(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        helper.assertTrue(DashService.tryDash(player, 1, 0) == DashService.Result.NOT_BOUGHT, "dash not bought");
        SkillTreeService.setPoints(player, Masteries.BODY, 2);
        SkillTreeService.buy(player, Ids.id("dash"));
        helper.assertTrue(DashService.tryDash(player, 1, 0) == DashService.Result.OK, "dash");
        helper.assertTrue(DashService.invulnerable(player), "invulnerable right after");
        helper.assertTrue(DashService.tryDash(player, 1, 0) == DashService.Result.ON_COOLDOWN, "cooldown");
        DashService.forget(player);
        helper.succeed();
    }
}
