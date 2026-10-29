package com.navesdev.grimoireknights.grimoire;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.registry.CoreRegistries;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GrimoireRegistries {
    public static final ResourceKey<Registry<Element>> ELEMENT = CoreRegistries.key("element");
    public static final Registry<Element> ELEMENTS = CoreRegistries.synced(ELEMENT);

    static final DeferredRegister<Element> ELEMENT_ENTRIES = DeferredRegister.create(ELEMENT, GrimoireKnights.MODID);

    public static final Supplier<Element> FIRE = ELEMENT_ENTRIES.register("fire", () -> new Element(List.of(Ids.id("fireball"))));

    private GrimoireRegistries() {}
}
