package com.navesdev.grimoireknights.ability;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.ability.spell.CrescentBlade;
import com.navesdev.grimoireknights.ability.spell.EmberBurst;
import com.navesdev.grimoireknights.ability.spell.Fireball;
import com.navesdev.grimoireknights.ability.spell.FlameArm;
import com.navesdev.grimoireknights.ability.spell.GaleBow;
import com.navesdev.grimoireknights.ability.spell.TornadoFang;
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
    public static final Supplier<Ability> FLAME_ARM = ABILITY_ENTRIES.register("flame_arm", FlameArm::new);
    public static final Supplier<Ability> EMBER_BURST = ABILITY_ENTRIES.register("ember_burst", EmberBurst::new);
    public static final Supplier<Ability> CRESCENT_BLADE = ABILITY_ENTRIES.register("crescent_blade", CrescentBlade::new);
    public static final Supplier<Ability> TORNADO_FANG = ABILITY_ENTRIES.register("tornado_fang", TornadoFang::new);
    public static final Supplier<Ability> GALE_BOW = ABILITY_ENTRIES.register("gale_bow", GaleBow::new);

    private AbilityRegistries() {}
}
