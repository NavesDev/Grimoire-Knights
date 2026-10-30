package com.navesdev.grimoireknights.grimoire.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Balance of the grimoire feature, in {@code grimoireknights-grimoire-server.toml}. */
public final class GrimoireConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue ELEMENTAL_WEIGHT;
    public static final ModConfigSpec.IntValue NO_MAGIC_WEIGHT;
    public static final ModConfigSpec.DoubleValue CALLING_MAX;
    public static final ModConfigSpec.DoubleValue CALLING_HEALTH_PER_POINT;
    public static final ModConfigSpec.IntValue PRIEST_ALTAR_RADIUS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("lottery");
        ELEMENTAL_WEIGHT = builder.comment("Lottery weight of each elemental grimoire; chance = weight / sum of weights.")
                .defineInRange("elementalWeight", 100, 1, 100_000);
        NO_MAGIC_WEIGHT = builder.comment("Lottery weight of having no magic (the forgotten grimoire quest).")
                .defineInRange("noMagicWeight", 20, 0, 100_000);
        builder.pop().push("calling");
        CALLING_MAX = builder.comment("Size of the hidden grimoire calling bar; the ceremony unlocks when it is full.")
                .defineInRange("max", 100.0, 1.0, 100_000.0);
        CALLING_HEALTH_PER_POINT = builder.comment("Health dealt to or taken from hostile mobs per calling point (2 = one heart).")
                .defineInRange("healthPerPoint", 2.0, 0.1, 1_000.0);
        builder.pop().push("ceremony");
        PRIEST_ALTAR_RADIUS = builder.comment("How far from the priest a Grimoire Altar may be for the ceremony.")
                .defineInRange("priestAltarRadius", 12, 1, 64);
        builder.pop();
        SPEC = builder.build();
    }

    private GrimoireConfig() {}
}
