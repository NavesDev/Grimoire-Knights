package com.navesdev.grimoireknights.progression;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.vfx.Vfx;
import com.navesdev.grimoireknights.grimoire.GrimoireActivity;
import com.navesdev.grimoireknights.progression.config.ProgressionConfig;
import com.navesdev.grimoireknights.progression.network.DashCooldownPayload;
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

    private DashService() {}

    public static Result tryDash(ServerPlayer player, double moveX, double moveZ) {
        if (!player.isAlive() || player.isSpectator() || player.isPassenger()) return deny(player, Result.CANNOT_ACT);
        if (SkillTreeService.effectRank(player, SkillEffects.DASH) <= 0) return deny(player, Result.NOT_BOUGHT);
        long now = player.serverLevel().getGameTime();
        if (READY_AT.getOrDefault(player.getUUID(), 0L) > now) return deny(player, Result.ON_COOLDOWN);
        // Client input is only a hint: clamp it to a unit vector.
        Vec3 look = player.getLookAngle();
        double[] dir = DashRules.direction(Double.isFinite(moveX) ? moveX : 0, Double.isFinite(moveZ) ? moveZ : 0, look.x, look.z);
        if (dir[0] == 0 && dir[1] == 0) return deny(player, Result.NO_DIRECTION);

        int ticks = ProgressionConfig.DASH_TICKS.get();
        int cooldown = ProgressionConfig.DASH_COOLDOWN_TICKS.get();
        READY_AT.put(player.getUUID(), now + cooldown);
        INVULNERABLE_UNTIL.put(player.getUUID(), now + ProgressionConfig.DASH_INVULNERABLE_TICKS.get());
        PUSHES.put(player.getUUID(), new Push(dir[0], dir[1], DashRules.speedPerTick(ProgressionConfig.DASH_DISTANCE.get(), ticks), now + ticks));
        push(player);
        if (CombatTracker.inCombat(player)) MasteryService.add(player, Masteries.BODY, ProgressionConfig.DASH_IN_COMBAT.get());
        Vfx.play(player.serverLevel(), Ids.id("dash"), player.position(), Map.of());
        GrimoireActivity.pose(player, Ids.id("dash"));
        PacketDistributor.sendToPlayer(player, new DashCooldownPayload(cooldown));
        return Result.OK;
    }

    public static void tick(ServerPlayer player) {
        Push active = PUSHES.get(player.getUUID());
        if (active == null) return;
        if (player.serverLevel().getGameTime() >= active.until() || !player.isAlive()) {
            PUSHES.remove(player.getUUID());
            player.setDeltaMovement(player.getDeltaMovement().multiply(0.3, 1, 0.3));
            player.hurtMarked = true;
            return;
        }
        push(player);
        Vfx.play(player.serverLevel(), Ids.id("wind_trail"), player.position().add(0, 1, 0), Map.of());
    }

    public static boolean invulnerable(ServerPlayer player) {
        return INVULNERABLE_UNTIL.getOrDefault(player.getUUID(), 0L) > player.serverLevel().getGameTime();
    }

    public static void forget(ServerPlayer player) {
        READY_AT.remove(player.getUUID());
        INVULNERABLE_UNTIL.remove(player.getUUID());
        PUSHES.remove(player.getUUID());
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
