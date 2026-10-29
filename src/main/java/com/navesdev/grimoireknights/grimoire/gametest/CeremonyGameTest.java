package com.navesdev.grimoireknights.grimoire.gametest;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.gametest.TestPlayers;
import com.navesdev.grimoireknights.grimoire.CeremonyService;
import com.navesdev.grimoireknights.grimoire.GrimoireModule;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import com.navesdev.grimoireknights.grimoire.attachment.GrimoireData;
import com.navesdev.grimoireknights.grimoire.entity.Priest;
import com.navesdev.grimoireknights.quest.QuestService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.animal.Pig;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GrimoireKnights.MODID)
@PrefixGameTestTemplate(false)
public class CeremonyGameTest {
    private static ServerPlayer player(GameTestHelper helper) {
        return TestPlayers.create(helper);
    }

    private static void done(GameTestHelper helper, ServerPlayer player) {
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ceremonyGivesAGrimoireOnlyOnce(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        CeremonyService.Result first = CeremonyService.start(player, null);
        GrimoireData after = GrimoireService.data(player);
        helper.assertTrue(after.ceremonyDone(), "ceremony marked done");
        if (first == CeremonyService.Result.GRIMOIRE) {
            helper.assertTrue(after.hasGrimoire() && after.leaves() == GrimoireService.CEREMONY_LEAVES, "grimoire with 3 leaves");
        } else {
            helper.assertTrue(first == CeremonyService.Result.NO_MAGIC, "grimoire or no magic, got " + first);
            helper.assertTrue(QuestService.data(player).active().containsKey(CeremonyService.FORGOTTEN_GRIMOIRE_QUEST), "forgotten grimoire quest given");
        }
        helper.assertTrue(CeremonyService.start(player, null) == CeremonyService.Result.ALREADY_DONE, "second ceremony denied");
        helper.assertTrue(GrimoireService.data(player).equals(after), "second ceremony changes nothing");
        done(helper, player);
    }

    @GameTest(template = "empty")
    public static void priestWaitsForTheCalling(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Priest priest = helper.spawn(GrimoireModule.PRIEST.get(), new BlockPos(2, 1, 2));
        CeremonyService.talk(player, priest);
        helper.assertFalse(GrimoireService.data(player).ceremonyDone(), "no ceremony before the calling is full");
        done(helper, player);
    }

    @GameTest(template = "empty")
    public static void priestHoldsTheCeremonyAtTheAltar(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        helper.setBlock(new BlockPos(2, 1, 2), GrimoireModule.ALTAR.get());
        Priest priest = helper.spawn(GrimoireModule.PRIEST.get(), new BlockPos(3, 1, 3));
        GrimoireService.setCalling(player, GrimoireService.callingMax());
        CeremonyService.talk(player, priest);
        helper.assertTrue(GrimoireService.data(player).ceremonyDone(), "ceremony held");
        done(helper, player);
    }

    @GameTest(template = "empty")
    public static void callingFillsOnlyFromHostileMobs(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(2, 1, 2));
        Pig pig = helper.spawn(EntityType.PIG, new BlockPos(1, 1, 1));
        pig.hurt(player.damageSources().playerAttack(player), 4);
        helper.assertTrue(GrimoireService.data(player).calling() == 0, "passive mobs do not count");
        float before = zombie.getHealth();
        zombie.hurt(player.damageSources().playerAttack(player), 4);
        double expected = (before - zombie.getHealth()) / 2.0;
        double calling = GrimoireService.data(player).calling();
        helper.assertTrue(expected > 0 && Math.abs(calling - expected) < 1e-6, "one point per heart taken from a hostile mob: expected " + expected + ", got " + calling);
        done(helper, player);
    }
}
