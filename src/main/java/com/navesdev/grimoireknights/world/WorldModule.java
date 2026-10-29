package com.navesdev.grimoireknights.world;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.command.GkCommands;
import com.navesdev.grimoireknights.grimoire.event.GrimoireCalledEvent;
import com.navesdev.grimoireknights.world.block.FloatingGrimoireBlock;
import com.navesdev.grimoireknights.world.block.FloatingGrimoireBlockEntity;
import com.navesdev.grimoireknights.world.command.TowerCommand;
import com.navesdev.grimoireknights.world.event.WorldEvents;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Starter village at spawn with the Grimoire Tower and church, its safe zone, and the calling's way to the tower. */
public final class WorldModule {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(GrimoireKnights.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GrimoireKnights.MODID);

    public static final DeferredBlock<FloatingGrimoireBlock> FLOATING_GRIMOIRE = BLOCKS.registerBlock("floating_grimoire", FloatingGrimoireBlock::new,
            BlockBehaviour.Properties.of().noCollission().strength(-1f, 3_600_000f).noLootTable().sound(SoundType.WOOL)
                    .lightLevel(state -> 6).pushReaction(PushReaction.BLOCK));
    public static final Supplier<BlockEntityType<FloatingGrimoireBlockEntity>> FLOATING_GRIMOIRE_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "floating_grimoire", () -> BlockEntityType.Builder.of(FloatingGrimoireBlockEntity::new, FLOATING_GRIMOIRE.get()).build(null));

    private WorldModule() {}

    public static void register(IEventBus modBus, ModContainer container) {
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        NeoForge.EVENT_BUS.addListener(LevelEvent.CreateSpawnPosition.class, WorldEvents::onCreateSpawn);
        NeoForge.EVENT_BUS.addListener(MobSpawnEvent.PositionCheck.class, WorldEvents::onSpawnCheck);
        NeoForge.EVENT_BUS.addListener(GrimoireCalledEvent.class, WorldEvents::onCalled);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> GkCommands.register(event.getDispatcher(), TowerCommand.branch()));
    }
}
