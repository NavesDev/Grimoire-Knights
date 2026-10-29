package com.navesdev.grimoireknights;

import com.mojang.logging.LogUtils;
import com.navesdev.grimoireknights.ability.AbilityModule;
import com.navesdev.grimoireknights.grimoire.GrimoireModule;
import com.navesdev.grimoireknights.quest.QuestModule;
import com.navesdev.grimoireknights.world.WorldModule;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(GrimoireKnights.MODID)
public class GrimoireKnights {
    public static final String MODID = "grimoireknights";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GrimoireKnights(IEventBus modBus, ModContainer container) {
        // Bottom-up, in dependency order (docs/arquitetura.md).
        QuestModule.register(modBus, container);
        GrimoireModule.register(modBus, container);
        AbilityModule.register(modBus, container);
        WorldModule.register(modBus, container);
    }
}
