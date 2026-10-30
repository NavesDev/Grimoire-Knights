package com.navesdev.grimoireknights.core;

import com.navesdev.grimoireknights.GrimoireKnights;
import net.minecraft.resources.ResourceLocation;

public final class Ids {
    private Ids() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(GrimoireKnights.MODID, path);
    }

    /** Translation key under the mod namespace, e.g. {@code key("command", "quest.given")}. */
    public static String key(String category, String path) {
        return category + "." + GrimoireKnights.MODID + "." + path;
    }
}
