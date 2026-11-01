package com.navesdev.grimoireknights;

import com.mojang.logging.LogUtils;
import com.navesdev.grimoireknights.ability.AbilityModule;
import com.navesdev.grimoireknights.core.DevLan;
import com.navesdev.grimoireknights.core.registry.CoreTabs;
import com.navesdev.grimoireknights.core.vfx.Vfx;
import com.navesdev.grimoireknights.cosmetic.CosmeticModule;
import com.navesdev.grimoireknights.gear.GearModule;
import com.navesdev.grimoireknights.grimoire.GrimoireModule;
import com.navesdev.grimoireknights.progression.ProgressionModule;
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
        // Bottom-up, in dependency order (docs/architecture.md).
        CoreTabs.register(modBus);
        DevLan.register();
        Vfx.register(modBus);
        QuestModule.register(modBus, container);
        GrimoireModule.register(modBus, container);
        ProgressionModule.register(modBus, container);
        AbilityModule.register(modBus, container);
        GearModule.register(modBus);
        CosmeticModule.register(modBus);
        WorldModule.register(modBus, container);
    }
}
