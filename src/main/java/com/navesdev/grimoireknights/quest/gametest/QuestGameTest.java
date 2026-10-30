package com.navesdev.grimoireknights.quest.gametest;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.gametest.TestPlayers;
import com.navesdev.grimoireknights.quest.QuestService;
import com.navesdev.grimoireknights.quest.event.QuestStartedEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GrimoireKnights.MODID)
@PrefixGameTestTemplate(false)
public class QuestGameTest {
    private static final ResourceLocation FORGOTTEN = Ids.id("forgotten_grimoire");

    @GameTest(template = "empty")
    public static void questStartsOnceAndCompletes(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        AtomicInteger started = new AtomicInteger();
        Consumer<QuestStartedEvent> listener = event -> {
            if (event.getPlayer() == player && event.getQuest().equals(FORGOTTEN)) started.incrementAndGet();
        };
        NeoForge.EVENT_BUS.addListener(listener);
        try {
            helper.assertTrue(QuestService.give(player, FORGOTTEN) == QuestService.GiveResult.OK, "quest given");
            helper.assertTrue(QuestService.give(player, FORGOTTEN) == QuestService.GiveResult.ALREADY_ACTIVE, "not given twice");
            helper.assertTrue(QuestService.give(player, Ids.id("missing")) == QuestService.GiveResult.UNKNOWN_QUEST, "unknown quest denied");
            helper.assertTrue(started.get() == 1, "started event once");
            helper.assertTrue(QuestService.complete(player, FORGOTTEN), "completed");
            helper.assertTrue(QuestService.data(player).completed().contains(FORGOTTEN), "marked completed");
            helper.assertTrue(QuestService.give(player, FORGOTTEN) == QuestService.GiveResult.ALREADY_COMPLETED, "does not repeat");
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
        }
        helper.succeed();
    }
}
