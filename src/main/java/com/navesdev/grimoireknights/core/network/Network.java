package com.navesdev.grimoireknights.core.network;

import com.navesdev.grimoireknights.GrimoireKnights;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Payload registration. Each feature listens to {@link RegisterPayloadHandlersEvent} and takes its registrar here.
 * Player data syncs through the attachment's own sync (see docs/architecture.md); payloads are for intents and one-off events.
 */
public final class Network {
    public static final String VERSION = "1";

    private Network() {}

    public static PayloadRegistrar registrar(RegisterPayloadHandlersEvent event) {
        return event.registrar(GrimoireKnights.MODID).versioned(VERSION);
    }
}
