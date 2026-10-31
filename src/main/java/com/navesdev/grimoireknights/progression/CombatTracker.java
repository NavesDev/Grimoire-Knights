package com.navesdev.grimoireknights.progression;

import com.navesdev.grimoireknights.progression.config.ProgressionConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

/** Last hit given or taken by each player, for gains that only count in combat. */
public final class CombatTracker {
    private static final Map<UUID, Long> LAST_HIT = new HashMap<>();

    private CombatTracker() {}

    public static void hit(ServerPlayer player) {
        LAST_HIT.put(player.getUUID(), player.serverLevel().getGameTime());
    }

    public static boolean inCombat(ServerPlayer player) {
        Long last = LAST_HIT.get(player.getUUID());
        return last != null && player.serverLevel().getGameTime() - last <= ProgressionConfig.COMBAT_TICKS.get();
    }

    public static void forget(ServerPlayer player) {
        LAST_HIT.remove(player.getUUID());
    }
}
