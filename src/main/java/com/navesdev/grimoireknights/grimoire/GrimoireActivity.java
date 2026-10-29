package com.navesdev.grimoireknights.grimoire;

import com.navesdev.grimoireknights.grimoire.network.GrimoireActivityPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/** Shows the floating grimoire to everyone near when its owner fights or casts. */
public final class GrimoireActivity {
    private static final int COMBAT_RESEND_TICKS = 20;
    private static final Map<UUID, Long> LAST_COMBAT = new HashMap<>();

    private GrimoireActivity() {}

    public static void combat(ServerPlayer player) {
        if (!GrimoireService.data(player).hasGrimoire()) return;
        long now = player.serverLevel().getGameTime();
        Long last = LAST_COMBAT.get(player.getUUID());
        if (last != null && now - last < COMBAT_RESEND_TICKS) return;
        LAST_COMBAT.put(player.getUUID(), now);
        send(player, GrimoireActivityPayload.Kind.COMBAT);
    }

    /** Called by {@code ability} after a successful cast: the grimoire turns a page. */
    public static void cast(ServerPlayer player) {
        if (!GrimoireService.data(player).hasGrimoire()) return;
        send(player, GrimoireActivityPayload.Kind.CAST);
    }

    static void forget(ServerPlayer player) {
        LAST_COMBAT.remove(player.getUUID());
    }

    private static void send(ServerPlayer player, GrimoireActivityPayload.Kind kind) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new GrimoireActivityPayload(player.getId(), kind));
    }
}
