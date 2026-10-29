package com.navesdev.grimoireknights.ability.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.navesdev.grimoireknights.ability.entity.FireballProjectile;
import com.navesdev.grimoireknights.core.client.FixedGeoModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Fireball, full bright, with its trail (+z in the model) behind the flight direction. */
class FireballRenderer extends GeoEntityRenderer<FireballProjectile> {
    FireballRenderer(EntityRendererProvider.Context context) {
        super(context, new FixedGeoModel<>("ability", "fireball", "textures/entity/ability/fireball.png"));
    }

    @Override
    protected void applyRotations(FireballProjectile fireball, PoseStack pose, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        // Projectiles face (sin yRot, cos yRot); the model faces -z.
        pose.mulPose(Axis.YP.rotationDegrees(180 + Mth.lerp(partialTick, fireball.yRotO, fireball.getYRot())));
        pose.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTick, fireball.xRotO, fireball.getXRot())));
    }

    @Override
    protected int getBlockLightLevel(FireballProjectile fireball, BlockPos pos) {
        return 15;
    }
}
