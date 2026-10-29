package com.navesdev.grimoireknights.world.client;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.client.FixedGeoModel;
import com.navesdev.grimoireknights.world.WorldModule;
import com.navesdev.grimoireknights.world.block.FloatingGrimoireBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public final class WorldClientModule {
    /** Covers the tower's grimoires show, picked by position. */
    private static final String[] COVERS = {"fire_3", "wind_3", "fire_4", "wind_3", "fire_3", "wind_4", "anti_5"};

    private WorldClientModule() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> event.registerBlockEntityRenderer(
                WorldModule.FLOATING_GRIMOIRE_BLOCK_ENTITY.get(), context -> new GeoBlockRenderer<>(new FixedGeoModel<FloatingGrimoireBlockEntity>(
                        "grimoire", "grimoire", "textures/entity/grimoire/grimoire_fire_3.png") {
                    @Override
                    public ResourceLocation getTextureResource(FloatingGrimoireBlockEntity grimoire) {
                        int index = Math.floorMod(grimoire.getBlockPos().hashCode(), COVERS.length);
                        return Ids.id("textures/entity/grimoire/grimoire_" + COVERS[index] + ".png");
                    }
                })));
    }
}
