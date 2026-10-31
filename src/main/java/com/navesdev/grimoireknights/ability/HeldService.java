package com.navesdev.grimoireknights.ability;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

/** Targets grabbed by the flame arm or caught by the tornado: they can't move, attack or cast until released. */
public final class HeldService {
    private static final Map<UUID, Long> UNTIL = new ConcurrentHashMap<>();

    private HeldService() {}

    public static void hold(Entity target, long untilGameTime) {
        UNTIL.put(target.getUUID(), untilGameTime);
    }

    public static void release(Entity target) {
        UNTIL.remove(target.getUUID());
    }

    public static boolean held(Entity entity) {
        Long until = UNTIL.get(entity.getUUID());
        if (until == null) return false;
        if (entity.level().getGameTime() < until && entity.isAlive()) return true;
        UNTIL.remove(entity.getUUID());
        return false;
    }

    /** Keeps the target at {@code x, y, z} with no motion; players are moved by the server since they own their movement. */
    public static void pin(Entity target, double x, double y, double z) {
        target.setDeltaMovement(0, 0, 0);
        target.fallDistance = 0;
        if (target instanceof ServerPlayer player) player.teleportTo(x, y, z);
        else target.setPos(x, y, z);
        if (target instanceof Mob mob) mob.getNavigation().stop();
        target.hurtMarked = true;
    }
}
