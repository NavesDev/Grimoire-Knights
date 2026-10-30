package com.navesdev.grimoireknights.grimoire.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.navesdev.grimoireknights.grimoire.GrimoireModule;
import com.navesdev.grimoireknights.grimoire.block.GrimoireAltarBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Draws the altar item with the block's GeckoLib model. */
class AltarItemRenderer extends BlockEntityWithoutLevelRenderer {
    private GrimoireAltarBlockEntity altar;

    AltarItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (altar == null) altar = new GrimoireAltarBlockEntity(BlockPos.ZERO, GrimoireModule.ALTAR.get().defaultBlockState());
        Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(altar, pose, buffers, light, overlay);
    }
}
