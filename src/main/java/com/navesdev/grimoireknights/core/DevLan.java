package com.navesdev.grimoireknights.core;

import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Development only: the integrated server always checks Mojang sessions, so the offline dev accounts
 * ({@code runClient2}'s Dev2) can't join a world opened to LAN. Turning the check off lets them in.
 */
public final class DevLan {
    private DevLan() {}

    public static void register() {
        if (!FMLEnvironment.production) {
            NeoForge.EVENT_BUS.addListener(DevLan::onServerStarted);
        }
    }

    private static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        if (!server.isDedicatedServer()) {
            server.setUsesAuthentication(false);
        }
    }
}
