package com.navesdev.grimoireknights.ability.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Balance of abilities and mana, in {@code grimoireknights-ability-server.toml}. Values to tune in playtests. */
public final class AbilityConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue MANA_BASE;
    public static final ModConfigSpec.DoubleValue MANA_PER_POINT;
    public static final ModConfigSpec.DoubleValue MANA_REGEN_PER_SECOND;
    public static final ModConfigSpec.DoubleValue FIREBALL_COST;
    public static final ModConfigSpec.DoubleValue FIREBALL_DAMAGE;
    public static final ModConfigSpec.IntValue FIREBALL_COOLDOWN_TICKS;
    public static final ModConfigSpec.DoubleValue FIREBALL_SPEED;
    public static final ModConfigSpec.IntValue FIREBALL_LIFETIME_TICKS;
    public static final ModConfigSpec.IntValue BURN_MAX_STACKS;
    public static final ModConfigSpec.DoubleValue BURN_DAMAGE_PER_STACK;
    public static final ModConfigSpec.IntValue BURN_DURATION_TICKS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("mana");
        MANA_BASE = builder.comment("Maximum mana with the Mana stat at 0.").defineInRange("base", 100.0, 0.0, 100_000.0);
        MANA_PER_POINT = builder.comment("Maximum mana added by each Mana stat point.").defineInRange("perPoint", 10.0, 0.0, 10_000.0);
        MANA_REGEN_PER_SECOND = builder.comment("Current mana regenerated per second.").defineInRange("regenPerSecond", 5.0, 0.0, 10_000.0);
        builder.pop().push("fireball");
        FIREBALL_COST = builder.comment("Mana spent per fireball.").defineInRange("cost", 20.0, 0.0, 100_000.0);
        FIREBALL_DAMAGE = builder.comment("Damage on hit (2 = one heart).").defineInRange("damage", 6.0, 0.0, 1_000.0);
        FIREBALL_COOLDOWN_TICKS = builder.comment("Cooldown in ticks (20 = 1 s).").defineInRange("cooldownTicks", 20, 0, 72_000);
        FIREBALL_SPEED = builder.comment("Blocks per tick.").defineInRange("speed", 1.5, 0.1, 10.0);
        FIREBALL_LIFETIME_TICKS = builder.comment("Ticks before a fireball that hit nothing fades.").defineInRange("lifetimeTicks", 60, 1, 1_200);
        builder.pop().push("burn");
        BURN_MAX_STACKS = builder.comment("Fire's burn: maximum stacks; each fireball hit adds one.").defineInRange("maxStacks", 5, 1, 100);
        BURN_DAMAGE_PER_STACK = builder.comment("Burn damage per second for each stack.").defineInRange("damagePerStack", 0.5, 0.0, 100.0);
        BURN_DURATION_TICKS = builder.comment("Ticks the burn lasts after the last hit.").defineInRange("durationTicks", 100, 1, 72_000);
        builder.pop();
        SPEC = builder.build();
    }

    private AbilityConfig() {}
}
