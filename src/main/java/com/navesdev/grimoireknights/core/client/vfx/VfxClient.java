package com.navesdev.grimoireknights.core.client.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.vfx.VfxPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Plays Blockbench effect sequences in the world: from the server's {@link VfxPayload} or from client code. */
public final class VfxClient {
    /** A frame longer than this (a hitch, a load) is not simulated as one big step. */
    private static final double MAX_STEP = 0.1;

    private record Playing(List<VfxLibrary.Cue> pending, Vec3 at, Map<String, Float> variables, double[] clock) {}

    private static final List<Playing> SEQUENCES = new ArrayList<>();
    private static final List<VfxEmitter> EMITTERS = new ArrayList<>();
    private static final Map<ResourceLocation, RenderType> ADDITIVE = new HashMap<>();
    private static final Set<ResourceLocation> MISSING = new HashSet<>();
    private static long lastFrame;

    private VfxClient() {}

    public static void register(IEventBus modBus) {
        VfxPayload.CLIENT.set(payload -> play(payload.effect(), new Vec3(payload.x(), payload.y(), payload.z()), payload.variables()));
        modBus.addListener(RegisterClientReloadListenersEvent.class, event -> event.registerReloadListener(new VfxLibrary()));
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.class, VfxClient::render);
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> {
            SEQUENCES.clear();
            EMITTERS.clear();
        });
    }

    /** Plays the sequence {@code <ns>:<name>} (animation {@code animation.vfx.<name>}) at {@code at}. */
    public static void play(ResourceLocation effect, Vec3 at, Map<String, Float> variables) {
        List<VfxLibrary.Cue> cues = VfxLibrary.sequence(effect);
        if (cues == null) {
            if (MISSING.add(effect)) GrimoireKnights.LOGGER.warn("No VFX sequence {}", effect);
            return;
        }
        SEQUENCES.add(new Playing(new ArrayList<>(cues), at, Map.copyOf(variables), new double[1]));
    }

    private static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        long now = System.nanoTime();
        double dt = lastFrame == 0 || minecraft.isPaused() ? 0 : Math.min(MAX_STEP, (now - lastFrame) / 1e9);
        lastFrame = now;
        if (level == null || (SEQUENCES.isEmpty() && EMITTERS.isEmpty())) return;

        Iterator<Playing> sequences = SEQUENCES.iterator();
        while (sequences.hasNext()) {
            Playing playing = sequences.next();
            playing.clock()[0] += dt;
            playing.pending().removeIf(cue -> {
                if (cue.time() > playing.clock()[0]) return false;
                EMITTERS.add(new VfxEmitter(cue.effect(), playing.at(), playing.variables()));
                return true;
            });
            if (playing.pending().isEmpty()) sequences.remove();
        }

        PoseStack.Pose pose = event.getPoseStack().last();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        Iterator<VfxEmitter> emitters = EMITTERS.iterator();
        while (emitters.hasNext()) {
            VfxEmitter emitter = emitters.next();
            emitter.update(dt);
            if (emitter.done()) {
                emitters.remove();
                continue;
            }
            emitter.render(pose, buffers.getBuffer(renderType(emitter.effect())), event.getCamera(), level);
        }
        buffers.endBatch();
    }

    private static RenderType renderType(VfxEffect effect) {
        return switch (effect.material()) {
            case ALPHA -> RenderType.entityCutoutNoCull(effect.texture());
            case BLEND -> RenderType.entityTranslucent(effect.texture());
            // Blockbench's particles_add: source alpha added on top (SRC_ALPHA, ONE), full bright.
            case ADD -> ADDITIVE.computeIfAbsent(effect.texture(), texture -> RenderType.EYES.apply(texture, RenderStateShard.LIGHTNING_TRANSPARENCY));
        };
    }
}
