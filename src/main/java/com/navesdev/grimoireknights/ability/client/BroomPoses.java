package com.navesdev.grimoireknights.ability.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.navesdev.grimoireknights.ability.entity.BroomEntity;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.grimoire.client.PlayerPoses;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Riders stand on the broom (player_ride poses): hover, or lean toward where the broom moves relative to their facing.
 * They are drawn turned with the broom, not with the camera.
 */
final class BroomPoses {
    /** Ticks to blend from one lean into the next. */
    private static final int FADE = 6;
    private static final Map<UUID, ResourceLocation> CURRENT = new HashMap<>();
    private static Player locked;
    private static float[] saved;

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
        return Ids.id("broom_" + broom.lean(player.getYRot()).name().toLowerCase(Locale.ROOT));
    }

    /**
     * Before a rider is drawn: body and head take the broom's yaw and the head no pitch, so the pose stays on the broom
     * however the camera turns, and the rider banks with the broom. {@link #unlock} puts it all back after drawing.
     */
    static void lock(Player player, float partialTick, PoseStack pose) {
        if (!(player.getVehicle() instanceof BroomEntity broom)) return;
        locked = player;
        // banked with the broom around its handle, so the feet stay on it
        Vec3 pivot = broom.getPosition(partialTick).subtract(player.getPosition(partialTick));
        pose.pushPose();
        pose.translate(pivot.x, pivot.y, pivot.z);
        pose.mulPose(broom.bank(partialTick));
        pose.translate(-pivot.x, -pivot.y, -pivot.z);
        saved = new float[] {player.yBodyRot, player.yBodyRotO, player.yHeadRot, player.yHeadRotO, player.getXRot(), player.xRotO};
        float yaw = Mth.rotLerp(partialTick, broom.yRotO, broom.getYRot());
        player.yBodyRot = player.yBodyRotO = player.yHeadRot = player.yHeadRotO = yaw;
        player.setXRot(0);
        player.xRotO = 0;
    }

    static void unlock(Player player, PoseStack pose) {
        if (player != locked) return;
        pose.popPose();
        player.yBodyRot = saved[0];
        player.yBodyRotO = saved[1];
        player.yHeadRot = saved[2];
        player.yHeadRotO = saved[3];
        player.setXRot(saved[4]);
        player.xRotO = saved[5];
        locked = null;
    }
}
