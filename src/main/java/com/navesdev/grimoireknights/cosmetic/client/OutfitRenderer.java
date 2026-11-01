package com.navesdev.grimoireknights.cosmetic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.cosmetic.Outfit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/** Draws an outfit piece on the armor model, posed like the wearer, like a vanilla armor piece of that slot. */
public class OutfitRenderer implements ICurioRenderer {
    private final EquipmentSlot slot;
    private final ResourceLocation texture;
    private HumanoidModel<LivingEntity> model;

    public OutfitRenderer(Outfit outfit, EquipmentSlot slot) {
        this.slot = slot;
        this.texture = Ids.id("textures/models/outfit/" + outfit.id + (slot == EquipmentSlot.LEGS ? "_layer_2.png" : "_layer_1.png"));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext context, PoseStack poseStack, RenderLayerParent<T, M> parent,
            MultiBufferSource buffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(parent.getModel() instanceof HumanoidModel<?> wearer)) return;
        HumanoidModel<LivingEntity> piece = model();
        ((HumanoidModel<LivingEntity>) wearer).copyPropertiesTo(piece);
        piece.setAllVisible(false);
        switch (slot) {
            case HEAD -> {
                piece.head.visible = true;
                piece.hat.visible = true;
            }
            case CHEST -> {
                piece.body.visible = true;
                piece.rightArm.visible = true;
                piece.leftArm.visible = true;
            }
            case LEGS -> {
                piece.body.visible = true;
                piece.rightLeg.visible = true;
                piece.leftLeg.visible = true;
            }
            default -> {
                piece.rightLeg.visible = true;
                piece.leftLeg.visible = true;
            }
        }
        piece.renderToBuffer(poseStack, buffer.getBuffer(RenderType.armorCutoutNoCull(texture)), light, OverlayTexture.NO_OVERLAY);
    }

    private HumanoidModel<LivingEntity> model() {
        if (model == null) {
            model = new HumanoidModel<>(Minecraft.getInstance().getEntityModels()
                    .bakeLayer(slot == EquipmentSlot.LEGS ? ModelLayers.PLAYER_INNER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR));
        }
        return model;
    }
}
