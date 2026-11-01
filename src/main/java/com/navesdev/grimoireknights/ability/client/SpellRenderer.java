package com.navesdev.grimoireknights.ability.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.navesdev.grimoireknights.ability.entity.BroomEntity;
import com.navesdev.grimoireknights.ability.entity.SpellProjectile;
import com.navesdev.grimoireknights.ability.entity.TornadoEntity;
import com.navesdev.grimoireknights.core.client.FixedGeoModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Spell entities: full bright, facing their flight (models face -z) and scaled by the spell. The broom takes its
 * rider's yaw, which turns the other way from a projectile's, and banks into strafes. The tornado model lies along its flight axis and is stood
 * up here, mouth up and tail on the ground: tornadoes are never sideways. Wind is drawn translucent
 * without culling (docs/models.md); {@code upgradeBones} are hidden until the projectile's upgrade reaches their level.
 */
class SpellRenderer<T extends net.minecraft.world.entity.Entity & GeoEntity> extends GeoEntityRenderer<T> {
    private static final float TORNADO_TAIL = 11 / 16f;
    private static final float TORNADO_AXIS = 8 / 16f;
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
        float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        if (entity instanceof BroomEntity broom) {
            // banks around its flight axis (world space), the top toward the strafe like its rider
            Vec3 facing = Vec3.directionFromRotation(0, yaw);
            pose.mulPose(new Quaternionf().rotationAxis(broom.roll(partialTick) * Mth.DEG_TO_RAD, (float) facing.x, 0, (float) facing.z));
        }
        pose.mulPose(Axis.YP.rotationDegrees(entity instanceof BroomEntity ? 180 - yaw : 180 + yaw));
        if (entity instanceof TornadoEntity) {
            // tail (z 11 pixels) on the ground, axis (y 8 pixels) on the entity's vertical, mouth (-z) up
            pose.translate(0, TORNADO_TAIL, 0);
            pose.mulPose(Axis.XP.rotationDegrees(90));
            pose.translate(0, -TORNADO_AXIS, 0);
            return;
        }
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
