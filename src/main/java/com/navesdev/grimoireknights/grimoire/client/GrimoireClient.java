package com.navesdev.grimoireknights.grimoire.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.client.vfx.VfxClient;
import com.navesdev.grimoireknights.grimoire.Element;
import com.navesdev.grimoireknights.grimoire.GrimoireModule;
import com.navesdev.grimoireknights.grimoire.GrimoireRegistries;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import software.bernie.geckolib.renderer.GeoObjectRenderer;

/**
 * Floating grimoires of the players around: state from the server's activity and ceremony payloads, drawn in the world.
 * Your own stays out of first person (it would cover the view), except while it flies to you at the ceremony.
 */
final class GrimoireClient {
    /** Where the grimoire floats, in model units from the player's feet: +x is the player's left, -z forward (grimoire.js FLOAT_AT). */
    private static final Vec3 FLOAT_AT = new Vec3(12, 14, -3).scale(1 / 16.0);
    /** The ceremony flight ends this high above the float spot, where {@code choose} starts (grimoire.js choose). */
    private static final double CHOOSE_START = 12 / 16.0;
    /** The book's middle above its float spot (model bone book, y 8), where the sparkle bursts. */
    private static final double BOOK_CENTER = 8 / 16.0;
    private static final ResourceLocation SPARKLE = Ids.id("grimoire_sparkle");

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
                }
            } else {
                grimoire.texture = data.element().map(element -> FloatingGrimoireModel.texture(element, data.leaves())).orElse(null);
                if (!data.hasGrimoire()) {
                    it.remove();
                    continue;
                }
            }
            if (grimoire.takeSparkle() && grimoire.texture != null && (!firstPerson(player) || grimoire.inFlight())) {
                sparkle(player, grimoire, ceremony != null ? ceremony.element() : data.element().orElse(null));
            }
        }
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
            if (firstPerson(player) && !grimoire.inFlight()) continue;
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

    private static boolean firstPerson(Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        return player == minecraft.getCameraEntity() && minecraft.options.getCameraType().isFirstPerson();
    }

    /** Sparkle in the element's color where the book is: at the shelves when the ceremony flight starts, else beside the player. */
    private static void sparkle(Player player, FloatingGrimoire grimoire, ResourceLocation elementId) {
        FloatingGrimoire.Ceremony ceremony = grimoire.ceremony();
        Vec3 at = ceremony != null && grimoire.inFlight() ? Vec3.atCenterOf(ceremony.from())
                : floatSpot(player.position(), player.yBodyRot).add(0, BOOK_CENTER, 0);
        int color = elementId == null ? 0xFFFFFF : GrimoireRegistries.ELEMENTS.getOptional(elementId).map(Element::color).orElse(0xFFFFFF);
        VfxClient.play(SPARKLE, at, Map.of("tint_r", (color >> 16 & 255) / 255f, "tint_g", (color >> 8 & 255) / 255f, "tint_b", (color & 255) / 255f));
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
