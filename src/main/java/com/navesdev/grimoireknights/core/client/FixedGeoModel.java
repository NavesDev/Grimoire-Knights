package com.navesdev.grimoireknights.core.client;

import com.navesdev.grimoireknights.core.Ids;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model at the mod's paths ({@code geo/<feature>/<name>.geo.json}, {@code animations/<feature>/<name>.animation.json},
 * a texture), drawn cutout so textures with holes (flames, wings) work.
 */
public class FixedGeoModel<T extends GeoAnimatable> extends GeoModel<T> {
    private final ResourceLocation model;
    private final ResourceLocation animation;
    private final ResourceLocation texture;

    public FixedGeoModel(String feature, String name, String texture) {
        this.model = Ids.id("geo/" + feature + "/" + name + ".geo.json");
        this.animation = Ids.id("animations/" + feature + "/" + name + ".animation.json");
        this.texture = Ids.id(texture);
    }

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return animation;
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
