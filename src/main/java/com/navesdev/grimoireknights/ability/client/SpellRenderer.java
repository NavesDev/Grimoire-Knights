package com.navesdev.grimoireknights.ability.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.navesdev.grimoireknights.ability.entity.SpellProjectile;
import com.navesdev.grimoireknights.core.client.FixedGeoModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Spell entities: full bright, facing their flight (models face -z) and scaled by the spell. Wind is drawn translucent
 * without culling (docs/models.md); {@code upgradeBones} are hidden until the projectile's upgrade reaches their level.
 */
class SpellRenderer<T extends net.minecraft.world.entity.Entity & GeoEntity> extends GeoEntityRenderer<T> {
    private final boolean translucent;
    private final float size;
    private final String[] upgradeBones;

    SpellRenderer(EntityRendererProvider.Context context, String model, boolean translucent, float size, String... upgradeBones) {
        super(context, new FixedGeoModel<>("ability", model, "textures/entity/ability/" + model + ".png"));
        this.translucent = translucent;
        this.size = size;
        this.upgradeBones = upgradeBones;
    }

    @Override
    protected void applyRotations(T entity, PoseStack pose, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        float scale = size * (entity instanceof SpellProjectile spell ? spell.scale() : 1);
        pose.scale(scale, scale, scale);
        pose.mulPose(Axis.YP.rotationDegrees(180 + Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        pose.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
    }

    @Override
    public void preRender(PoseStack pose, T entity, BakedGeoModel model, MultiBufferSource buffers, com.mojang.blaze3d.vertex.VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        int upgrade = entity instanceof SpellProjectile spell ? spell.upgrade() : 0;
        for (int i = 0; i < upgradeBones.length; i++) {
            int needed = i + 1;
            model.getBone(upgradeBones[i]).ifPresent(bone -> bone.setHidden(upgrade < needed));
        }
        super.preRender(pose, entity, model, buffers, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }

    @Override
    public RenderType getRenderType(T entity, ResourceLocation texture, MultiBufferSource buffers, float partialTick) {
        return translucent ? RenderType.entityTranslucent(texture) : RenderType.entityCutoutNoCull(texture);
    }

    @Override
    protected int getBlockLightLevel(T entity, BlockPos pos) {
        return 15;
    }
}
