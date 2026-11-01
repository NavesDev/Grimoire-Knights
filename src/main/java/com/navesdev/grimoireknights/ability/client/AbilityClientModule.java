package com.navesdev.grimoireknights.ability.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.ability.AbilityModule;
import com.navesdev.grimoireknights.ability.network.CastAbilityPayload;
import com.navesdev.grimoireknights.ability.network.TailwindPayload;
import com.navesdev.grimoireknights.core.Ids;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class AbilityClientModule {
    static final String CATEGORY = "key.categories." + GrimoireKnights.MODID;
    /** Slot keys Z, X, C (rebindable; X and C also load/save creative toolbars in vanilla). */
    public static final KeyMapping[] ABILITIES = {
            new KeyMapping(Ids.key("key", "ability_1"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY),
            new KeyMapping(Ids.key("key", "ability_2"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, CATEGORY),
            new KeyMapping(Ids.key("key", "ability_3"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY)};

    private AbilityClientModule() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterKeyMappingsEvent.class, event -> {
            for (KeyMapping key : ABILITIES) event.register(key);
        });
        TailwindPayload.CLIENT.set(payload -> ManaHud.tailwind = payload.stacks());
        modBus.addListener(RegisterGuiLayersEvent.class, event -> event.registerAbove(VanillaGuiLayers.HOTBAR, Ids.id("mana"), ManaHud::render));
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> {
            event.registerEntityRenderer(AbilityModule.FIREBALL.get(), FireballRenderer::new);
            event.registerEntityRenderer(AbilityModule.FLAME_FIST.get(), context -> new SpellRenderer<>(context, "flame_fist", false, 1.0f));
            event.registerEntityRenderer(AbilityModule.WIND_BLADE.get(), context -> new SpellRenderer<>(context, "wind_blade", true, 1.0f));
            event.registerEntityRenderer(AbilityModule.TORNADO.get(), context -> new SpellRenderer<>(context, "tornado", true, 2.0f, "blades", "stones"));
            event.registerEntityRenderer(AbilityModule.WIND_ARROW.get(), context -> new SpellRenderer<>(context, "wind_arrow", true, 1.0f));
            event.registerEntityRenderer(AbilityModule.WIND_BOW.get(), context -> new SpellRenderer<>(context, "wind_bow", true, 1.0f));
            event.registerEntityRenderer(AbilityModule.BROOM.get(), context -> new SpellRenderer<>(context, "broom", false, 1.0f));
        });
        NeoForge.EVENT_BUS.addListener(RenderPlayerEvent.Pre.class, event -> BroomPoses.lock(event.getEntity(), event.getPartialTick()));
        NeoForge.EVENT_BUS.addListener(RenderPlayerEvent.Post.class, event -> BroomPoses.unlock(event.getEntity()));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
            BroomPoses.tick();
            for (int slot = 0; slot < ABILITIES.length; slot++) {
                while (ABILITIES[slot].consumeClick()) {
                    if (Minecraft.getInstance().player != null) PacketDistributor.sendToServer(new CastAbilityPayload(slot));
                }
            }
        });
    }
}
