package com.navesdev.grimoireknights.grimoire.client;

import com.navesdev.grimoireknights.core.client.FixedGeoModel;
import com.navesdev.grimoireknights.core.Ids;
import net.minecraft.resources.ResourceLocation;

/** Grimoire model; the cover texture follows the element and leaves ({@code grimoire_<element>_<leaves>}). */
class FloatingGrimoireModel extends FixedGeoModel<FloatingGrimoire> {
    FloatingGrimoireModel() {
        super("grimoire", "grimoire", "textures/entity/grimoire/grimoire_fire_3.png");
    }

    static ResourceLocation texture(ResourceLocation element, int leaves) {
        return Ids.id("textures/entity/grimoire/grimoire_" + element.getPath() + "_" + leaves + ".png");
    }

    @Override
    public ResourceLocation getTextureResource(FloatingGrimoire animatable) {
        return animatable.texture != null ? animatable.texture : super.getTextureResource(animatable);
    }
}
