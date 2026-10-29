package com.navesdev.grimoireknights;

import com.navesdev.grimoireknights.ability.client.AbilityClientModule;
import com.navesdev.grimoireknights.grimoire.client.GrimoireClientModule;
import com.navesdev.grimoireknights.world.client.WorldClientModule;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = GrimoireKnights.MODID, dist = Dist.CLIENT)
public class GrimoireKnightsClient {
    public GrimoireKnightsClient(IEventBus modBus) {
        GrimoireClientModule.register(modBus);
        AbilityClientModule.register(modBus);
        WorldClientModule.register(modBus);
    }
}
