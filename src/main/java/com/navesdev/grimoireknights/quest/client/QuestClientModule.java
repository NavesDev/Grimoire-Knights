package com.navesdev.grimoireknights.quest.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.quest.network.DialoguePayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

/** Quest journal key (J), the NPC dialogue screen and the HUD tracker. */
public final class QuestClientModule {
    static final KeyMapping JOURNAL = new KeyMapping(Ids.key("key", "quest_journal"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J,
            "key.categories." + GrimoireKnights.MODID);

    private QuestClientModule() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterKeyMappingsEvent.class, event -> event.register(JOURNAL));
        modBus.addListener(RegisterGuiLayersEvent.class, event -> {
            event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, Ids.id("quest_tracker"), QuestTrackerHud::render);
            event.registerAbove(VanillaGuiLayers.HOTBAR, Ids.id("journal_hint"), JournalHint::render);
        });
        DialoguePayload.CLIENT.set(payload -> Minecraft.getInstance().setScreen(new DialogueScreen(payload)));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
            Minecraft minecraft = Minecraft.getInstance();
            while (JOURNAL.consumeClick()) {
                if (minecraft.player != null && minecraft.screen == null) minecraft.setScreen(new QuestJournalScreen());
            }
        });
    }
}
