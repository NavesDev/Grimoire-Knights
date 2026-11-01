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

    public static final Supplier<Element> FIRE = ELEMENT_ENTRIES.register("fire",
            () -> new Element(0xFF7A12, List.of(Ids.id("fireball"), Ids.id("flame_arm"), Ids.id("ember_burst"))));
    public static final Supplier<Element> WIND = ELEMENT_ENTRIES.register("wind",
            () -> new Element(0xE8F4FF, List.of(Ids.id("crescent_blade"), Ids.id("tornado_fang"), Ids.id("gale_bow"))));

    private GrimoireRegistries() {}
}
