package com.navesdev.grimoireknights.grimoire.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.grimoire.GrimoireModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoObjectRenderer;

/** Draws the grimoire icon item with the grimoire model and the cover its {@code grimoire_cover} names. */
class GrimoireItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final String DEFAULT_COVER = "fire_3";
    private final FloatingGrimoire grimoire = new FloatingGrimoire(-1);
    private GeoObjectRenderer<FloatingGrimoire> renderer;

    GrimoireItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (renderer == null) renderer = new GeoObjectRenderer<>(new FloatingGrimoireModel());
        Minecraft minecraft = Minecraft.getInstance();
        grimoire.tick(minecraft.level != null ? minecraft.level.getGameTime() : 0);
        grimoire.texture = Ids.id("textures/entity/grimoire/grimoire_" + stack.getOrDefault(GrimoireModule.GRIMOIRE_COVER.get(), DEFAULT_COVER) + ".png");
        renderer.render(pose, grimoire, buffers, null, null, light, minecraft.getTimer().getGameTimeDeltaPartialTick(false));
    }
}
