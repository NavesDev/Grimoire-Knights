package com.navesdev.grimoireknights.world.event;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.grimoire.event.GrimoireCalledEvent;
import com.navesdev.grimoireknights.world.WorldStructures;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.phys.Vec3;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

public final class WorldEvents {
    /** Natural spawns blocked in the safe zone; commands, eggs and spawners still work. */
    private static final Set<MobSpawnType> NATURAL = Set.of(MobSpawnType.NATURAL, MobSpawnType.CHUNK_GENERATION);
    private static final int TRAIL_LENGTH = 10;
    private static final int SPAWN_RADIUS = 4;
    private static final int TOWER_SEARCH_CHUNKS = 100;

    private WorldEvents() {}

    /** New world: spawn on the plaza of the starter village, by the Grimoire Tower. */
    public static void onCreateSpawn(LevelEvent.CreateSpawnPosition event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) return;
        var sets = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
        var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(WorldStructures.STARTER_VILLAGE);
        var set = sets.get(WorldStructures.STARTER_VILLAGE_SET);
        if (set == null || structure == null) {
            GrimoireKnights.LOGGER.warn("Starter village missing from the datapacks; vanilla spawn");
            return;
        }
        StructurePlacement placement = set.placement();
        if (!(placement instanceof ConcentricRingsStructurePlacement rings)) return;
        List<ChunkPos> positions = level.getChunkSource().getGeneratorState().getRingPositionsFor(rings);
        if (positions == null || positions.isEmpty()) return;
        ChunkPos chunk = positions.getFirst();
        StructureStart start = level.getChunk(chunk.x, chunk.z, ChunkStatus.STRUCTURE_STARTS).getStartForStructure(structure);
        if (start == null || !start.isValid() || start.getPieces().isEmpty()) {
            GrimoireKnights.LOGGER.warn("Starter village did not generate at {}; vanilla spawn", chunk);
            return;
        }
        // The first piece is the plaza with the tower; its border is a path, outside the tower walls.
        BoundingBox plaza = start.getPieces().getFirst().getBoundingBox();
        int x = plaza.getCenter().getX();
        int z = plaza.minZ() + 1;
        level.getChunk(x >> 4, z >> 4);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos spawn = new BlockPos(x, y, z);
        event.getSettings().setSpawn(spawn, 0f);
        // Spread players over the plaza only: the tower's overhanging top room starts 6 blocks away, out of this radius.
        level.getGameRules().getRule(GameRules.RULE_SPAWN_RADIUS).set(SPAWN_RADIUS, level.getServer());
        // Cancelling skips vanilla's bonus chest, so place it here when the world asked for one.
        if (level.getServer().getWorldData().worldGenOptions().generateBonusChest()) {
            level.registryAccess().registry(Registries.CONFIGURED_FEATURE)
                    .flatMap(features -> features.getHolder(MiscOverworldFeatures.BONUS_CHEST))
                    .ifPresent(feature -> feature.value().place(level, level.getChunkSource().getGenerator(), level.random, spawn));
        }
        event.setCanceled(true);
        GrimoireKnights.LOGGER.info("Spawn set at the starter village, {} {} {}", x, y, z);
    }

    /** Safe zone: hostile mobs do not spawn naturally inside the starter village. */
    public static void onSpawnCheck(MobSpawnEvent.PositionCheck event) {
        if (event.getEntity() instanceof Enemy && NATURAL.contains(event.getSpawnType())
                && inStarterVillage(event.getLevel().getLevel(), event.getEntity().blockPosition())) {
            event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
        }
    }

    /** Pillager patrols skip the position check; they are stopped here. */
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (event.getSpawnType() == MobSpawnType.PATROL && event.getEntity() instanceof Enemy
                && inStarterVillage(event.getLevel().getLevel(), event.getEntity().blockPosition())) {
            event.setSpawnCancelled(true);
        }
    }

    /** Phantoms skip the position check too. */
    public static void onPhantoms(PlayerSpawnPhantomsEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && inStarterVillage(player.serverLevel(), player.blockPosition())) {
            event.setResult(PlayerSpawnPhantomsEvent.Result.DENY);
        }
    }

    /** No raids in the safe zone: Bad Omen turns into Raid Omen only outside the starter village. */
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().is(MobEffects.RAID_OMEN) && event.getEntity() instanceof ServerPlayer player
                && inStarterVillage(player.serverLevel(), player.blockPosition())) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    private static boolean inStarterVillage(ServerLevel level, BlockPos pos) {
        Structure village = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(WorldStructures.STARTER_VILLAGE);
        return village != null && level.structureManager().getStructureWithPieceAt(pos, village).isValid();
    }

    /** The calling: a trail of particles from the player toward the nearest Grimoire Tower. */
    public static void onCalled(GrimoireCalledEvent event) {
        ServerPlayer player = event.getPlayer();
        ServerLevel level = player.serverLevel();
        BlockPos tower = level.findNearestMapStructure(WorldStructures.GRIMOIRE_TOWERS, player.blockPosition(), TOWER_SEARCH_CHUNKS, false);
        if (tower == null) return;
        Vec3 from = player.getEyePosition();
        Vec3 dir = new Vec3(tower.getX() + 0.5 - from.x, 0, tower.getZ() + 0.5 - from.z);
        if (dir.lengthSqr() < 1) return;
        dir = dir.normalize();
        for (int i = 1; i <= TRAIL_LENGTH; i++) {
            Vec3 at = from.add(dir.scale(i));
            level.sendParticles(player, ParticleTypes.ENCHANT, true, at.x, at.y - 0.3, at.z, 8, 0.15, 0.15, 0.15, 0.2);
            level.sendParticles(player, ParticleTypes.END_ROD, true, at.x, at.y - 0.3, at.z, 1, 0, 0, 0, 0);
        }
    }
}
