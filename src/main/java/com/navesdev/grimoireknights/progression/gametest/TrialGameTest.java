package com.navesdev.grimoireknights.progression.gametest;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.gametest.TestPlayers;
import com.navesdev.grimoireknights.grimoire.GrimoireModule;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import com.navesdev.grimoireknights.progression.MasteryService;
import com.navesdev.grimoireknights.progression.RankService;
import com.navesdev.grimoireknights.progression.rules.Rank;
import com.navesdev.grimoireknights.quest.QuestService;
import com.navesdev.grimoireknights.quest.objective.DefeatObjective;
import com.navesdev.grimoireknights.quest.objective.TalkObjective;
import java.util.Optional;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GrimoireKnights.MODID)
@PrefixGameTestTemplate(false)
public class TrialGameTest {
    private static final ResourceLocation FIRE = Ids.id("fire");
    private static final ResourceLocation FIRST_TRIAL = Ids.id("trial/renegade_captain");

    @GameTest(template = "empty")
    public static void theTrialPromotesAndTheMasteryResumes(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        GrimoireService.give(player, FIRE, GrimoireService.CEREMONY_LEAVES);
        MasteryService.add(player, FIRE, 1e9);
        helper.assertTrue(MasteryService.get(player, FIRE).level() == 10, "stopped at the recruit ceiling");
        helper.assertTrue(QuestService.offerFrom(player, Ids.id("priest")).equals(Optional.of(FIRST_TRIAL)), "the priest offers the first trial");
        helper.assertTrue(QuestService.forceComplete(player, FIRST_TRIAL), "completed by command");
        helper.assertTrue(RankService.rank(player) == Rank.KNIGHT, "rank_up reward promotes");
        MasteryService.add(player, FIRE, 1e9);
        helper.assertTrue(MasteryService.get(player, FIRE).level() == 20, "resumes up to the Knight ceiling");
        helper.assertTrue(QuestService.offerFrom(player, Ids.id("priest")).equals(Optional.of(Ids.id("trial/eclipse_twins"))), "then the next trial");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void onlyTheOfferedQuestCanBeAccepted(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        helper.assertTrue(QuestService.answer(player, FIRST_TRIAL, true) == QuestService.GiveResult.UNKNOWN_QUEST, "nothing offered, nothing accepted");
        QuestService.say(player, player, net.minecraft.network.chat.Component.empty(), Optional.of(FIRST_TRIAL));
        helper.assertTrue(QuestService.answer(player, Ids.id("trial/host"), true) == QuestService.GiveResult.UNKNOWN_QUEST, "another quest is refused");
        QuestService.say(player, player, net.minecraft.network.chat.Component.empty(), Optional.of(FIRST_TRIAL));
        helper.assertTrue(QuestService.answer(player, FIRST_TRIAL, true) == QuestService.GiveResult.OK, "the offered one is accepted");
        helper.assertTrue(QuestService.data(player).active().containsKey(FIRST_TRIAL), "and starts");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void objectivesMatchTheirEvents(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(1.5, 1, 1.5));
        helper.assertTrue(new DefeatObjective(Optional.of(EntityType.ZOMBIE), Optional.empty(), 2, Optional.empty()).onKill(player, zombie) == 1, "defeat by type");
        helper.assertTrue(new DefeatObjective(Optional.of(EntityType.SKELETON), Optional.empty(), 2, Optional.empty()).onKill(player, zombie) == 0, "other type");
        helper.assertTrue(new TalkObjective(GrimoireModule.PRIEST.get()).onTalk(player, GrimoireModule.PRIEST.get()) == 1, "talk to the priest");
        helper.succeed();
    }
}
