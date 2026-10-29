package com.navesdev.grimoireknights.core.registry;

import com.navesdev.grimoireknights.core.Ids;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

/** Creates the mod's own registries. Each feature owns its key and type; core only builds them. */
public final class CoreRegistries {
    private CoreRegistries() {}

    public static <T> ResourceKey<Registry<T>> key(String name) {
        return ResourceKey.createRegistryKey(Ids.id(name));
    }

    /** Registry synced to clients by id, so the network only carries ids. */
    public static <T> Registry<T> synced(ResourceKey<Registry<T>> key) {
        return new RegistryBuilder<>(key).sync(true).create();
    }

    /** Registry that only lives on the server side of the logic (codecs, types). */
    public static <T> Registry<T> local(ResourceKey<Registry<T>> key) {
        return new RegistryBuilder<>(key).create();
    }

    public static void register(IEventBus modBus, Registry<?>... registries) {
        modBus.addListener(NewRegistryEvent.class, event -> {
            for (Registry<?> registry : registries) event.register(registry);
        });
    }
}
