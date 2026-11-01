package com.navesdev.grimoireknights.ability.client;

import com.navesdev.grimoireknights.ability.entity.BroomEntity;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.grimoire.client.PlayerPoses;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** Riders stand on the broom (player_ride poses): hover, or lean toward where the broom moves relative to their facing. */
final class BroomPoses {
    private static final double MOVING = 0.01;
    /** Ticks to blend from one lean into the next. */
    private static final int FADE = 6;
    private static final Map<UUID, ResourceLocation> CURRENT = new HashMap<>();

    private BroomPoses() {}

    static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            CURRENT.clear();
            return;
        }
        for (AbstractClientPlayer player : minecraft.level.players()) {
            ResourceLocation wanted = player.getVehicle() instanceof BroomEntity broom ? pose(player, broom) : null;
            ResourceLocation current = CURRENT.get(player.getUUID());
            if (wanted == null) {
                if (current != null) {
                    PlayerPoses.stop(player);
                    CURRENT.remove(player.getUUID());
                }
            } else if (!wanted.equals(current)) {
                PlayerPoses.play(player, wanted, current == null ? 0 : FADE);
                CURRENT.put(player.getUUID(), wanted);
            }
        }
        CURRENT.keySet().removeIf(id -> minecraft.level.getPlayerByUUID(id) == null);
    }

    private static ResourceLocation pose(AbstractClientPlayer player, BroomEntity broom) {
        Vec3 motion = broom.getDeltaMovement();
        if (motion.horizontalDistanceSqr() < MOVING) return Ids.id("broom_hover");
        Vec3 facing = Vec3.directionFromRotation(0, player.getYRot());
        double forward = motion.x * facing.x + motion.z * facing.z;
        double side = motion.x * -facing.z + motion.z * facing.x;
        if (Math.abs(side) > Math.abs(forward)) return Ids.id(side > 0 ? "broom_right" : "broom_left");
        return Ids.id(forward >= 0 ? "broom_forward" : "broom_backward");
    }
}
