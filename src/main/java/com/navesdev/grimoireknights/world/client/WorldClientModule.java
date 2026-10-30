package com.navesdev.grimoireknights.world.client;

import com.navesdev.grimoireknights.world.WorldModule;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class WorldClientModule {
    private WorldClientModule() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> event.registerBlockEntityRenderer(
                WorldModule.FLOATING_GRIMOIRE_BLOCK_ENTITY.get(), context -> new FloatingGrimoireRenderer()));
    }
}
