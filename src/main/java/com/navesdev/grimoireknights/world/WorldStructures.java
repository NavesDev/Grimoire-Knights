package com.navesdev.grimoireknights.world;

import com.navesdev.grimoireknights.core.Ids;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public final class WorldStructures {
    public static final ResourceKey<Structure> STARTER_VILLAGE = ResourceKey.create(Registries.STRUCTURE, Ids.id("starter_village"));
    public static final ResourceKey<StructureSet> STARTER_VILLAGE_SET = ResourceKey.create(Registries.STRUCTURE_SET, Ids.id("starter_village"));
    /** Structures with a Grimoire Tower; the calling points to the nearest. */
    public static final TagKey<Structure> GRIMOIRE_TOWERS = TagKey.create(Registries.STRUCTURE, Ids.id("grimoire_tower"));
    public static final ResourceLocation TOWER_TEMPLATE = Ids.id("tower/grimoire_tower");

    private WorldStructures() {}
}
