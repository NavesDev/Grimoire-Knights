package com.navesdev.grimoireknights.grimoire;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

/**
 * A grimoire element. Abilities are kept by id, one per slot (Z, X, C), so this feature does not depend on {@code ability}.
 * {@code color} (0xRRGGBB) tints the element's effects, like the grimoire's sparkle.
 */
public record Element(int color, List<ResourceLocation> abilities) {
    public Element {
        abilities = List.copyOf(abilities);
    }

    public Optional<ResourceLocation> ability(int slot) {
        return slot >= 0 && slot < abilities.size() ? Optional.of(abilities.get(slot)) : Optional.empty();
    }
}
