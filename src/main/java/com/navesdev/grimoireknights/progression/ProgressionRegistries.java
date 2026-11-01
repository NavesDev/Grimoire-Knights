package com.navesdev.grimoireknights.progression;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.registry.CoreRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public final class ProgressionRegistries {
    public static final ResourceKey<Registry<SkillNode>> SKILL_NODE = CoreRegistries.key("skill_node");
    public static final DataMapType<EntityType<?>, MasteryValue> MASTERY = DataMapType.builder(Ids.id("mastery"), Registries.ENTITY_TYPE, MasteryValue.CODEC).build();
    public static final DataMapType<EntityType<?>, CombatLevel> COMBAT_LEVEL = DataMapType.builder(Ids.id("combat_level"), Registries.ENTITY_TYPE, CombatLevel.CODEC).build();
    /** Damage types that count as spells: they train the element mastery instead of Body. */
    public static final TagKey<DamageType> SPELL = TagKey.create(Registries.DAMAGE_TYPE, Ids.id("spell"));

    private ProgressionRegistries() {}
}
