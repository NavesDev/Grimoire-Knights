package com.navesdev.grimoireknights.grimoire.client;

import com.navesdev.grimoireknights.core.client.FixedGeoModel;
import com.navesdev.grimoireknights.grimoire.GrimoireModule;
import com.navesdev.grimoireknights.grimoire.block.GrimoireAltarBlockEntity;
import com.navesdev.grimoireknights.grimoire.entity.Priest;
import com.navesdev.grimoireknights.grimoire.network.CeremonyPayload;
import com.navesdev.grimoireknights.grimoire.network.ElementChoicePayload;
import com.navesdev.grimoireknights.grimoire.network.GrimoireActivityPayload;
import com.navesdev.grimoireknights.grimoire.network.PlayerAnimationPayload;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class GrimoireClientModule {
    private GrimoireClientModule() {}

    public static void register(IEventBus modBus) {
        GrimoireActivityPayload.CLIENT.set(GrimoireClient::onActivity);
        CeremonyPayload.CLIENT.set(GrimoireClient::onCeremony);
        PlayerAnimationPayload.CLIENT.set(PlayerPoses::onPlay);
        ElementChoicePayload.CLIENT.set(ElementChoiceScreen::open);

        modBus.addListener(FMLClientSetupEvent.class, event -> event.enqueueWork(PlayerPoses::init));
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> {
            event.registerBlockEntityRenderer(GrimoireModule.ALTAR_BLOCK_ENTITY.get(), context -> new GeoBlockRenderer<GrimoireAltarBlockEntity>(
                    new FixedGeoModel<>("building", "grimoire_altar", "textures/block/building/grimoire_altar.png")));
            event.registerEntityRenderer(GrimoireModule.PRIEST.get(), context -> new GeoEntityRenderer<Priest>(context,
                    new FixedGeoModel<>("npc", "priest", "textures/entity/npc/priest.png")));
        });
        modBus.addListener(RegisterClientExtensionsEvent.class, event -> event.registerItem(new IClientItemExtensions() {
            private AltarItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new AltarItemRenderer();
                return renderer;
            }
        }, GrimoireModule.ALTAR_ITEM.get()));
        modBus.addListener(RegisterClientExtensionsEvent.class, event -> event.registerItem(new IClientItemExtensions() {
            private GrimoireItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new GrimoireItemRenderer();
                return renderer;
            }
        }, GrimoireModule.GRIMOIRE_ICON.get()));

        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, GrimoireClient::tick);
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.class, GrimoireClient::render);
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> GrimoireClient.clear());
    }
}
