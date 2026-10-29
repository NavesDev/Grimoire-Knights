package com.navesdev.grimoireknights.grimoire.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.grimoire.GrimoireModule;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import com.navesdev.grimoireknights.grimoire.attachment.GrimoireData;
import com.navesdev.grimoireknights.grimoire.network.CeremonyPayload;
import com.navesdev.grimoireknights.grimoire.network.GrimoireActivityPayload;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import software.bernie.geckolib.renderer.GeoObjectRenderer;

/** Floating grimoires of the players around: state from the server's activity and ceremony payloads, drawn in the world. */
final class GrimoireClient {
    /** Where the grimoire floats, in model units from the player's feet: +x is the player's left, -z forward (grimoire.js FLOAT_AT). */
    private static final Vec3 FLOAT_AT = new Vec3(12, 14, -3).scale(1 / 16.0);
    /** The ceremony flight ends this high above the float spot, where {@code choose} starts (grimoire.js choose). */
    private static final double CHOOSE_START = 12 / 16.0;

    private static final Map<Integer, FloatingGrimoire> GRIMOIRES = new HashMap<>();
    private static GeoObjectRenderer<FloatingGrimoire> renderer;

    private GrimoireClient() {}

    static void onActivity(GrimoireActivityPayload payload) {
        FloatingGrimoire grimoire = get(payload.entityId());
        switch (payload.kind()) {
            case COMBAT -> grimoire.showFor(FloatingGrimoire.VISIBLE_TICKS);
            case CAST -> grimoire.flip();
        }
    }

    static void onCeremony(CeremonyPayload payload) {
        get(payload.entityId()).startCeremony(new FloatingGrimoire.Ceremony(payload.from(), now(), payload.element(), payload.leaves()));
    }

    static void clear() {
        GRIMOIRES.clear();
    }

    private static FloatingGrimoire get(int entityId) {
        FloatingGrimoire grimoire = GRIMOIRES.computeIfAbsent(entityId, FloatingGrimoire::new);
        grimoire.tick(now());
        return grimoire;
    }

    private static long now() {
        ClientLevel level = Minecraft.getInstance().level;
        return level != null ? level.getGameTime() : 0;
    }

    static void tick(ClientTickEvent.Post event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            GRIMOIRES.clear();
            return;
        }
        long now = level.getGameTime();
        Iterator<FloatingGrimoire> it = GRIMOIRES.values().iterator();
        while (it.hasNext()) {
            FloatingGrimoire grimoire = it.next();
            grimoire.tick(now);
            Entity entity = level.getEntity(grimoire.entityId);
            if (!(entity instanceof Player player) || (!grimoire.visible() && grimoire.ceremony() == null)) {
                it.remove();
                continue;
            }
            GrimoireData data = player.getData(GrimoireModule.GRIMOIRE_DATA);
            FloatingGrimoire.Ceremony ceremony = grimoire.ceremony();
            if (ceremony != null) {
                grimoire.texture = FloatingGrimoireModel.texture(ceremony.element(), ceremony.leaves());
                if (now - ceremony.start() >= FloatingGrimoire.CEREMONY_FLIGHT_TICKS + FloatingGrimoire.CEREMONY_CHOOSE_TICKS) {
                    grimoire.endCeremony();
                    if (player == Minecraft.getInstance().player) reveal(ceremony);
                }
            } else {
                grimoire.texture = data.element().map(element -> FloatingGrimoireModel.texture(element, data.leaves())).orElse(null);
                if (!data.hasGrimoire()) it.remove();
            }
        }
    }

    private static void reveal(FloatingGrimoire.Ceremony ceremony) {
        Minecraft minecraft = Minecraft.getInstance();
        Component element = GrimoireService.elementName(ceremony.element());
        minecraft.gui.setTitle(Component.translatable(Ids.key("title", "ceremony.chosen")));
        minecraft.gui.setSubtitle(Component.translatable(Ids.key("title", "ceremony.element"), element));
    }

    static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || GRIMOIRES.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) return;
        if (renderer == null) renderer = new GeoObjectRenderer<>(new FloatingGrimoireModel());
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        for (FloatingGrimoire grimoire : GRIMOIRES.values()) {
            if (!grimoire.visible() || grimoire.texture == null) continue;
            if (!(level.getEntity(grimoire.entityId) instanceof Player player) || player.isInvisible() || player.isSpectator()) continue;
            float yaw = Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot);
            Vec3 spot = floatSpot(player.getPosition(partial), yaw);
            Vec3 at = spot;
            FloatingGrimoire.Ceremony ceremony = grimoire.ceremony();
            if (ceremony != null && grimoire.inFlight()) {
                Vec3 from = Vec3.atCenterOf(ceremony.from());
                Vec3 to = spot.add(0, CHOOSE_START, 0);
                double t = Mth.clamp((grimoire.now() - ceremony.start() + partial) / FloatingGrimoire.CEREMONY_FLIGHT_TICKS, 0, 1);
                at = from.lerp(to, t * t * (3 - 2 * t));
            }
            pose.pushPose();
            pose.translate(at.x - camera.x, at.y - camera.y, at.z - camera.z);
            pose.mulPose(Axis.YP.rotationDegrees(180 - yaw));
            // GeoObjectRenderer centers on a block (0.5, 0.51, 0.5); the model origin is already where it should be.
            pose.translate(-0.5, -0.51, -0.5);
            int light = LevelRenderer.getLightColor(level, BlockPos.containing(at));
            renderer.render(pose, grimoire, buffers, null, null, light, partial);
            pose.popPose();
        }
        buffers.endBatch();
    }

    /** FLOAT_AT turned to the player's facing: forward is (-sin, cos), left is (cos, sin) of the yaw. */
    private static Vec3 floatSpot(Vec3 feet, float yaw) {
        double rad = Math.toRadians(yaw);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);
        double left = FLOAT_AT.x;
        double forward = -FLOAT_AT.z;
        return feet.add(cos * left - sin * forward, FLOAT_AT.y, sin * left + cos * forward);
    }
}
