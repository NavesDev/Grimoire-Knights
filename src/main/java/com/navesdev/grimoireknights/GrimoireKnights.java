package com.navesdev.grimoireknights;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(GrimoireKnights.MODID)
public class GrimoireKnights {
    public static final String MODID = "grimoireknights";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GrimoireKnights(IEventBus modEventBus) {
        LOGGER.info("Grimoire Knights loading");
    }
}
