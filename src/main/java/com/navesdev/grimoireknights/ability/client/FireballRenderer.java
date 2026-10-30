package com.navesdev.grimoireknights.ability.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.navesdev.grimoireknights.ability.entity.FireballProjectile;
import com.navesdev.grimoireknights.core.client.FixedGeoModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.Color;

/** Fireball, full bright, with its trail (+z in the model) behind the flight direction; fades and shrinks at the end of its range. */
class FireballRenderer extends GeoEntityRenderer<FireballProjectile> {
    private static final float CENTER = 4 / 16f;

    FireballRenderer(EntityRendererProvider.Context context) {
        super(context, new FixedGeoModel<>("ability", "fireball", "textures/entity/ability/fireball.png"));
    }

    @Override
    protected void applyRotations(FireballProjectile fireball, PoseStack pose, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        // Shrinks around the ball's center (model y 4), not the feet.
        float fade = fireball.fade(partialTick);
        pose.translate(0, CENTER, 0);
        pose.scale(fade, fade, fade);
        pose.translate(0, -CENTER, 0);
        // Projectiles face (sin yRot, cos yRot); the model faces -z.
        pose.mulPose(Axis.YP.rotationDegrees(180 + Mth.lerp(partialTick, fireball.yRotO, fireball.getYRot())));
        pose.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTick, fireball.xRotO, fireball.getXRot())));
    }

    @Override
    public Color getRenderColor(FireballProjectile fireball, float partialTick, int packedLight) {
        return Color.ofRGBA(1f, 1f, 1f, fireball.fade(partialTick));
    }

    @Override
    public RenderType getRenderType(FireballProjectile fireball, ResourceLocation texture, MultiBufferSource buffers, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    protected int getBlockLightLevel(FireballProjectile fireball, BlockPos pos) {
        return 15;
    }
}
