package com.navesdev.grimoireknights.progression;

import com.navesdev.grimoireknights.core.Ids;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

/** Mastery ids. Grimoire masteries use the element id itself, so each element keeps its own. */
public final class Masteries {
    public static final ResourceLocation BODY = Ids.id("body");
    public static final ResourceLocation MANA = Ids.id("mana");
    public static final ResourceLocation ANTIMANA = Ids.id("antimana");
    /** Not playable yet: only its masteries exist. */
    public static final ResourceLocation ANTI_MAGIC = Ids.id("anti_magic");

    private Masteries() {}

    /** Mana, or Antimana for anti-magic; Mana without a grimoire. */
    public static ResourceLocation resource(Optional<ResourceLocation> element) {
        return element.filter(ANTI_MAGIC::equals).map(e -> ANTIMANA).orElse(MANA);
    }
}
