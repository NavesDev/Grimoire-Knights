package com.navesdev.grimoireknights.cosmetic.client;

import com.navesdev.grimoireknights.cosmetic.CosmeticModule;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

/** Outfit renderers and the armor toggle in the inventory. */
public final class CosmeticClientModule {
    private CosmeticClientModule() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(FMLClientSetupEvent.class, event -> CosmeticModule.OUTFITS.forEach((outfit, pieces) ->
                pieces.forEach((slot, item) -> CuriosRendererRegistry.register(item.get(), () -> new OutfitRenderer(outfit, slot)))));
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Init.Post.class, event -> {
            if (event.getScreen() instanceof InventoryScreen screen) event.addListener(new ArmorLookButton(screen));
        });
    }
}
