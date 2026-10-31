package com.navesdev.grimoireknights.progression.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.progression.network.DashCooldownPayload;
import com.navesdev.grimoireknights.progression.network.DashPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Dash (R) and skill tree (K) keys, the tree screen and the dash cooldown the HUD shows. */
public final class ProgressionClientModule {
    private static final String CATEGORY = "key.categories." + GrimoireKnights.MODID;
    static final KeyMapping DASH = new KeyMapping(Ids.key("key", "dash"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);
    static final KeyMapping TREE = new KeyMapping(Ids.key("key", "skill_tree"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY);

    private static long dashReadyAt;
    private static int dashCooldown;

    private ProgressionClientModule() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterKeyMappingsEvent.class, event -> {
            event.register(DASH);
            event.register(TREE);
        });
        DashCooldownPayload.CLIENT.set(payload -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) return;
            dashCooldown = payload.ticks();
            dashReadyAt = minecraft.level.getGameTime() + payload.ticks();
        });
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            while (DASH.consumeClick()) {
                if (player != null) PacketDistributor.sendToServer(dashPayload(player));
            }
            while (TREE.consumeClick()) {
                if (player != null && minecraft.screen == null) minecraft.setScreen(new SkillTreeScreen());
            }
        });
    }

    /** 0 when ready, up to 1 right after a dash; the HUD darkens the dash icon by it. */
    public static float dashCooldownFraction() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || dashCooldown <= 0) return 0;
        return Mth.clamp((dashReadyAt - minecraft.level.getGameTime()) / (float) dashCooldown, 0, 1);
    }

    /** Movement keys turned into a world direction, as in vanilla {@code moveRelative}. */
    private static DashPayload dashPayload(LocalPlayer player) {
        float left = player.input.leftImpulse;
        float forward = player.input.forwardImpulse;
        float sin = Mth.sin(player.getYRot() * Mth.DEG_TO_RAD);
        float cos = Mth.cos(player.getYRot() * Mth.DEG_TO_RAD);
        return new DashPayload(left * cos - forward * sin, forward * cos + left * sin);
    }
}
