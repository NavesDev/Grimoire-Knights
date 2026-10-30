package com.navesdev.grimoireknights.core.config;

import com.navesdev.grimoireknights.GrimoireKnights;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/** One SERVER config per feature: {@code grimoireknights-<feature>-server.toml}, synced to clients. */
public final class Configs {
    private Configs() {}

    public static void registerServer(ModContainer container, String feature, ModConfigSpec spec) {
        container.registerConfig(ModConfig.Type.SERVER, spec, GrimoireKnights.MODID + "-" + feature + "-server.toml");
    }
}
