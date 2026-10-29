package com.navesdev.grimoireknights.ability.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.ability.AbilityModule;
import com.navesdev.grimoireknights.ability.network.CastAbilityPayload;
import com.navesdev.grimoireknights.core.Ids;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class AbilityClientModule {
    static final String CATEGORY = "key.categories." + GrimoireKnights.MODID;
    /** Slot keys Z, X, C; stage 1 only has magic 1. */
    static final KeyMapping ABILITY_1 = new KeyMapping(Ids.key("key", "ability_1"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY);

    private AbilityClientModule() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterKeyMappingsEvent.class, event -> event.register(ABILITY_1));
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class,
                event -> event.registerEntityRenderer(AbilityModule.FIREBALL.get(), FireballRenderer::new));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
            while (ABILITY_1.consumeClick()) {
                if (Minecraft.getInstance().player != null) PacketDistributor.sendToServer(new CastAbilityPayload(0));
            }
        });
    }
}
