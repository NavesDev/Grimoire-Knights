package com.navesdev.grimoireknights.progression.config;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Balance of masteries, the tree, ranks and the dash, in {@code grimoireknights-progression-server.toml}. Values to tune in playtests. */
public final class ProgressionConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue CURVE_BASE;
    public static final ModConfigSpec.DoubleValue CURVE_MULT;
    public static final ModConfigSpec.DoubleValue CURVE_EXP;
    public static final ModConfigSpec.IntValue MAX_LEVEL;
    public static final ModConfigSpec.DoubleValue FACTOR_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue FACTOR_EXP;
    public static final ModConfigSpec.DoubleValue DEFAULT_VALUE_PER_HEALTH;
    public static final ModConfigSpec.DoubleValue SPELL_DAMAGE_RATIO;
    public static final ModConfigSpec.DoubleValue PHYSICAL_DAMAGE_RATIO;
    public static final ModConfigSpec.DoubleValue DAMAGE_TAKEN_RATIO;
    public static final ModConfigSpec.DoubleValue DASH_IN_COMBAT;
    public static final ModConfigSpec.DoubleValue MANA_SPENT_RATIO;
    public static final ModConfigSpec.IntValue COMBAT_TICKS;
    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> RANK_CEILINGS;
    public static final ModConfigSpec.DoubleValue MAX_MANA_PER_RANK;
    public static final ModConfigSpec.DoubleValue REGEN_PER_RANK;
    public static final ModConfigSpec.DoubleValue MANA_CONTROL_DISCOUNT;
    public static final ModConfigSpec.DoubleValue DASH_DISTANCE;
    public static final ModConfigSpec.IntValue DASH_TICKS;
    public static final ModConfigSpec.IntValue DASH_COOLDOWN_TICKS;
    public static final ModConfigSpec.IntValue DASH_INVULNERABLE_TICKS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("mastery");
        CURVE_BASE = builder.comment("Progress to the next level = base + mult x level^exp.").defineInRange("curveBase", 20.0, 0.0, 1e6);
        CURVE_MULT = builder.defineInRange("curveMult", 5.0, 0.0, 1e6);
        CURVE_EXP = builder.defineInRange("curveExp", 1.5, 0.0, 10.0);
        MAX_LEVEL = builder.comment("Highest mastery level.").defineInRange("maxLevel", 50, 1, 1000);
        FACTOR_PER_LEVEL = builder.comment("Enemy strength factor = min(1, (enemy value / (perLevel x level))^exp).").defineInRange("factorPerLevel", 2.0, 0.01, 1e6);
        FACTOR_EXP = builder.defineInRange("factorExp", 2.0, 0.0, 10.0);
        DEFAULT_VALUE_PER_HEALTH = builder.comment("Mastery value of a mob missing from the data map, per point of max health.").defineInRange("defaultValuePerHealth", 0.5, 0.0, 100.0);
        SPELL_DAMAGE_RATIO = builder.comment("Element mastery per point of spell damage dealt.").defineInRange("spellDamageRatio", 0.5, 0.0, 100.0);
        PHYSICAL_DAMAGE_RATIO = builder.comment("Body mastery per point of physical damage dealt.").defineInRange("physicalDamageRatio", 0.5, 0.0, 100.0);
        DAMAGE_TAKEN_RATIO = builder.comment("Body mastery per point of damage taken from a living attacker.").defineInRange("damageTakenRatio", 0.5, 0.0, 100.0);
        DASH_IN_COMBAT = builder.comment("Body mastery for a dash while in combat.").defineInRange("dashInCombat", 2.0, 0.0, 1e6);
        MANA_SPENT_RATIO = builder.comment("Mana mastery per point of mana spent.").defineInRange("manaSpentRatio", 0.2, 0.0, 100.0);
        COMBAT_TICKS = builder.comment("Ticks after the last hit given or taken that still count as in combat.").defineInRange("combatTicks", 200, 0, 72_000);
        builder.pop().push("rank");
        RANK_CEILINGS = builder.comment("Grimoire mastery ceiling per rank: Recruit, Knight, Elite Knight, Captain, Magic King.")
                .defineList("ceilings", List.of(10, 20, 30, 40, 50), () -> 10, value -> value instanceof Integer i && i >= 1);
        builder.pop().push("tree");
        MAX_MANA_PER_RANK = builder.comment("Max mana added by each rank of a max_mana node.").defineInRange("maxManaPerRank", 10.0, 0.0, 1e6);
        REGEN_PER_RANK = builder.comment("Mana per second added by each rank of a mana_regen node.").defineInRange("regenPerRank", 1.0, 0.0, 1e6);
        MANA_CONTROL_DISCOUNT = builder.comment("Spell and flight mana cost cut by each Mana Control rank.").defineInRange("manaControlDiscount", 0.05, 0.0, 0.33);
        builder.pop().push("dash");
        DASH_DISTANCE = builder.comment("Blocks toward the movement.").defineInRange("distance", 5.0, 0.0, 64.0);
        DASH_TICKS = builder.comment("Ticks the push lasts.").defineInRange("ticks", 4, 1, 40);
        DASH_COOLDOWN_TICKS = builder.defineInRange("cooldownTicks", 60, 0, 72_000);
        DASH_INVULNERABLE_TICKS = builder.defineInRange("invulnerableTicks", 6, 0, 200);
        builder.pop();
        SPEC = builder.build();
    }

    private ProgressionConfig() {}
}
