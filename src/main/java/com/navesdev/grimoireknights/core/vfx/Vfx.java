package com.navesdev.grimoireknights.core.vfx;

import com.navesdev.grimoireknights.core.network.Network;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Visual effects made in Blockbench (docs/adr/0004-bedrock-particles.md). The server only asks: an effect is a
 * sequence in {@code assets/<ns>/vfx/animations/vfx.animation.json}; the client plays it.
 */
public final class Vfx {
    /** Players farther than this do not get the effect. */
    private static final double RANGE = 64;

    private Vfx() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> Network.registrar(event)
                .playToClient(VfxPayload.TYPE, VfxPayload.STREAM_CODEC, (payload, context) -> VfxPayload.CLIENT.accept(payload)));
    }

    public static void play(ServerLevel level, ResourceLocation effect, Vec3 at, Map<String, Float> variables) {
        PacketDistributor.sendToPlayersNear(level, null, at.x, at.y, at.z, RANGE, new VfxPayload(effect, at.x, at.y, at.z, variables));
    }
}
