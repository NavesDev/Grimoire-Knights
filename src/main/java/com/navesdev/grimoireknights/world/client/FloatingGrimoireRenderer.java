package com.navesdev.grimoireknights.world.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.client.FixedGeoModel;
import com.navesdev.grimoireknights.world.block.FloatingGrimoireBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** Draws a tower grimoire circling its block's column, so a column of them in the middle of the tower makes a slow whirl. */
public class FloatingGrimoireRenderer extends GeoBlockRenderer<FloatingGrimoireBlockEntity> {
    /** Covers the tower's grimoires show, picked by position. */
    private static final String[] COVERS = {"fire_3", "wind_3", "fire_4", "wind_3", "fire_3", "wind_4", "anti_5"};
    static final double RADIUS = 3.5;
    private static final float DEGREES_PER_TICK = 0.6f;
    private static final float GOLDEN_ANGLE = 137.5f;

    public FloatingGrimoireRenderer() {
        super(new FixedGeoModel<>("grimoire", "grimoire", "textures/entity/grimoire/grimoire_fire_3.png") {
            @Override
            public ResourceLocation getTextureResource(FloatingGrimoireBlockEntity grimoire) {
                int index = Math.floorMod(grimoire.getBlockPos().hashCode(), COVERS.length);
                return Ids.id("textures/entity/grimoire/grimoire_" + COVERS[index] + ".png");
            }
        });
    }

    @Override
    protected void rotateBlock(Direction facing, PoseStack poseStack) {
        BlockPos pos = animatable.getBlockPos();
        float time = animatable.getLevel().getGameTime() + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        // each height starts at its own angle, so the column spreads into a spiral
        float angle = pos.getY() * GOLDEN_ANGLE + time * DEGREES_PER_TICK;
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.translate(0, 0, RADIUS);
    }

    @Override
    public AABB getRenderBoundingBox(FloatingGrimoireBlockEntity grimoire) {
        return new AABB(grimoire.getBlockPos()).inflate(RADIUS + 1);
    }
}
