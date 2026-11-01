package com.navesdev.grimoireknights.progression;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.vfx.Vfx;
import com.navesdev.grimoireknights.grimoire.GrimoireActivity;
import com.navesdev.grimoireknights.progression.config.ProgressionConfig;
import com.navesdev.grimoireknights.progression.event.DashEvent;
import com.navesdev.grimoireknights.progression.network.DashCooldownPayload;
import net.neoforged.neoforge.common.NeoForge;
import com.navesdev.grimoireknights.progression.rules.DashRules;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/** The Body dash: needs its node, pushes the player over a few ticks and makes them invulnerable for a moment. */
public final class DashService {
    public enum Result { OK, CANNOT_ACT, NOT_BOUGHT, ON_COOLDOWN, NO_DIRECTION }

    private record Push(double x, double z, double speed, long until) {}

    private static final Map<UUID, Long> READY_AT = new HashMap<>();
    private static final Map<UUID, Long> INVULNERABLE_UNTIL = new HashMap<>();
    private static final Map<UUID, Push> PUSHES = new HashMap<>();
    /** Tick of the last sneak press (the key going down), for the roll's timing. */
    private static final Map<UUID, Long> SNEAK_PRESSED_AT = new HashMap<>();
    private static final Map<UUID, Boolean> WAS_SNEAKING = new HashMap<>();

    private DashService() {}

    public static Result tryDash(ServerPlayer player, double moveX, double moveZ) {
        if (!player.isAlive() || player.isSpectator() || player.isPassenger()) return deny(player, Result.CANNOT_ACT);
        if (SkillTreeService.effectRank(player, SkillEffects.DASH) <= 0) return deny(player, Result.NOT_BOUGHT);
        // higher features veto it (ability: held targets can't dash)
        if (NeoForge.EVENT_BUS.post(new DashEvent(player)).isCanceled()) return deny(player, Result.CANNOT_ACT);
        long now = player.serverLevel().getGameTime();
        if (READY_AT.getOrDefault(player.getUUID(), 0L) > now) return deny(player, Result.ON_COOLDOWN);
        // Client input is only a hint: clamp it to a unit vector.
        Vec3 look = player.getLookAngle();
        double[] dir = DashRules.direction(Double.isFinite(moveX) ? moveX : 0, Double.isFinite(moveZ) ? moveZ : 0, look.x, look.z);
        if (dir[0] == 0 && dir[1] == 0) return deny(player, Result.NO_DIRECTION);

        int cooldown = ProgressionConfig.DASH_COOLDOWN_TICKS.get();
        READY_AT.put(player.getUUID(), now + cooldown);
        INVULNERABLE_UNTIL.put(player.getUUID(), now + ProgressionConfig.DASH_INVULNERABLE_TICKS.get());
        start(player, dir, ProgressionConfig.DASH_DISTANCE.get(), ProgressionConfig.DASH_TICKS.get());
        if (CombatTracker.inCombat(player)) MasteryService.add(player, Masteries.BODY, ProgressionConfig.DASH_IN_COMBAT.get());
        // full id: the demon form has a looping flight pose also named "dash"
        GrimoireActivity.pose(player, Ids.id("animation.player_cast." + DashRules.pose(dir[0], dir[1], look.x, look.z)));
        PacketDistributor.sendToPlayer(player, new DashCooldownPayload(cooldown));
        return Result.OK;
    }

    /** Roll node: a sneak pressed just before landing from a fall turns it into a short dash toward the look, with no
     *  cooldown; the caller drops the fall damage. Each press rolls once. */
    public static boolean tryRoll(ServerPlayer player, float fallDistance) {
        if (player.isPassenger() || fallDistance < ProgressionConfig.ROLL_MIN_FALL.get()) return false;
        if (SkillTreeService.effectRank(player, SkillEffects.ROLL) <= 0) return false;
        watchSneak(player);
        long pressedAt = SNEAK_PRESSED_AT.getOrDefault(player.getUUID(), -1L);
        if (!DashRules.rollTimed(pressedAt, player.serverLevel().getGameTime(), ProgressionConfig.ROLL_WINDOW_TICKS.get())) return false;
        SNEAK_PRESSED_AT.remove(player.getUUID());
        Vec3 look = player.getLookAngle();
        double[] dir = DashRules.direction(0, 0, look.x, look.z);
        if (dir[0] == 0 && dir[1] == 0) return false;
        start(player, dir, ProgressionConfig.ROLL_DISTANCE.get(), ProgressionConfig.ROLL_TICKS.get());
        GrimoireActivity.pose(player, Ids.id("animation.player_cast.roll"));
        return true;
    }

    private static void start(ServerPlayer player, double[] dir, double distance, int ticks) {
        long now = player.serverLevel().getGameTime();
        PUSHES.put(player.getUUID(), new Push(dir[0], dir[1], DashRules.speedPerTick(distance, ticks), now + ticks));
        push(player);
        Vfx.play(player.serverLevel(), Ids.id("dash"), player.position(), Map.of("dir_x", (float) dir[0], "dir_z", (float) dir[1]));
    }

    public static void tick(ServerPlayer player) {
        watchSneak(player);
        Push active = PUSHES.get(player.getUUID());
        if (active == null) return;
        if (player.serverLevel().getGameTime() >= active.until() || !player.isAlive()) {
            PUSHES.remove(player.getUUID());
            player.setDeltaMovement(player.getDeltaMovement().multiply(0.3, 1, 0.3));
            player.hurtMarked = true;
            return;
        }
        push(player);
        Vfx.play(player.serverLevel(), Ids.id("dash_trail"), player.position(), Map.of("dir_x", (float) active.x(), "dir_z", (float) active.z()));
    }

    /** Notes the tick the sneak key went down; the fall hook calls it too, as it can run before this tick's player tick. */
    private static void watchSneak(ServerPlayer player) {
        boolean sneaking = player.isShiftKeyDown();
        if (sneaking && !WAS_SNEAKING.getOrDefault(player.getUUID(), false)) SNEAK_PRESSED_AT.put(player.getUUID(), player.serverLevel().getGameTime());
        WAS_SNEAKING.put(player.getUUID(), sneaking);
    }

    public static boolean invulnerable(ServerPlayer player) {
        return INVULNERABLE_UNTIL.getOrDefault(player.getUUID(), 0L) > player.serverLevel().getGameTime();
    }

    public static void forget(ServerPlayer player) {
        READY_AT.remove(player.getUUID());
        INVULNERABLE_UNTIL.remove(player.getUUID());
        PUSHES.remove(player.getUUID());
        SNEAK_PRESSED_AT.remove(player.getUUID());
        WAS_SNEAKING.remove(player.getUUID());
    }

    private static void push(ServerPlayer player) {
        Push active = PUSHES.get(player.getUUID());
        player.setDeltaMovement(active.x() * active.speed(), Math.max(0.05, player.getDeltaMovement().y), active.z() * active.speed());
        // tells the client, which owns its own movement
        player.hurtMarked = true;
    }

    private static Result deny(ServerPlayer player, Result result) {
        GrimoireKnights.LOGGER.debug("Dash denied for {}: {}", player.getGameProfile().getName(), result);
        return result;
    }
}
