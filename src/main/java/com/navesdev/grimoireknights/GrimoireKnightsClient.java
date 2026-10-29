package com.navesdev.grimoireknights;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = GrimoireKnights.MODID, dist = Dist.CLIENT)
public class GrimoireKnightsClient {
    public GrimoireKnightsClient() {
        GrimoireKnights.LOGGER.info("Grimoire Knights client loading");
    }
}
