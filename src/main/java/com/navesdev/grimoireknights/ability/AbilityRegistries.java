package com.navesdev.grimoireknights.ability;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.ability.spell.Fireball;
import com.navesdev.grimoireknights.core.registry.CoreRegistries;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AbilityRegistries {
    public static final ResourceKey<Registry<Ability>> ABILITY = CoreRegistries.key("ability");
    public static final Registry<Ability> ABILITIES = CoreRegistries.synced(ABILITY);

    static final DeferredRegister<Ability> ABILITY_ENTRIES = DeferredRegister.create(ABILITY, GrimoireKnights.MODID);

    public static final Supplier<Ability> FIREBALL = ABILITY_ENTRIES.register("fireball", Fireball::new);

    private AbilityRegistries() {}
}
