package com.navesdev.grimoireknights.progression.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Fired before a dash on the server; cancelling denies it. Lets higher features veto it without a dependency cycle. */
public class DashEvent extends PlayerEvent implements ICancellableEvent {
    public DashEvent(ServerPlayer player) {
        super(player);
    }
}
