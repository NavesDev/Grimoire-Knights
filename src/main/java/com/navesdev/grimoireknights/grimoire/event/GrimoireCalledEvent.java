package com.navesdev.grimoireknights.grimoire.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** The player's calling bar is full and the ceremony waits. Posted on {@code NeoForge.EVENT_BUS}, server side. */
public class GrimoireCalledEvent extends Event {
    private final ServerPlayer player;

    public GrimoireCalledEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() {
        return player;
    }
}
