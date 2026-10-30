package com.navesdev.grimoireknights.core.registry;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.Ids;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The mod's creative tab. Core does not know the items: each feature adds its own on
 * {@code BuildCreativeModeTabContentsEvent} for {@link #MAIN}.
 */
public final class CoreTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GrimoireKnights.MODID);
    public static final ResourceKey<CreativeModeTab> MAIN = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Ids.id("main"));
    /** Icon by id: the grimoire item belongs to the grimoire feature. */
    private static final Supplier<CreativeModeTab> MAIN_TAB = TABS.register(MAIN.location().getPath(), () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + GrimoireKnights.MODID + ".main"))
            .icon(() -> new ItemStack(BuiltInRegistries.ITEM.get(Ids.id("grimoire"))))
            .build());

    private CoreTabs() {}

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
