package com.navesdev.grimoireknights.grimoire;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.command.GkCommands;
import com.navesdev.grimoireknights.core.config.Configs;
import com.navesdev.grimoireknights.core.network.Network;
import com.navesdev.grimoireknights.core.registry.CoreRegistries;
import com.navesdev.grimoireknights.grimoire.attachment.GrimoireData;
import com.navesdev.grimoireknights.grimoire.block.GrimoireAltarBlock;
import com.navesdev.grimoireknights.grimoire.block.GrimoireAltarBlockEntity;
import com.navesdev.grimoireknights.grimoire.command.GrimoireCommand;
import com.navesdev.grimoireknights.grimoire.config.GrimoireConfig;
import com.navesdev.grimoireknights.grimoire.entity.Priest;
import com.navesdev.grimoireknights.grimoire.event.GrimoireEvents;
import com.navesdev.grimoireknights.grimoire.network.CeremonyPayload;
import com.navesdev.grimoireknights.grimoire.network.ChooseElementPayload;
import com.navesdev.grimoireknights.grimoire.network.ElementChoicePayload;
import com.navesdev.grimoireknights.grimoire.network.GrimoireActivityPayload;
import com.navesdev.grimoireknights.grimoire.network.PlayerAnimationPayload;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Grimoire: element, leaves, lottery, calling, ceremony (priest and Grimoire Altar), floating grimoire. */
public final class GrimoireModule {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, GrimoireKnights.MODID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(GrimoireKnights.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GrimoireKnights.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GrimoireKnights.MODID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, GrimoireKnights.MODID);

    public static final Supplier<AttachmentType<GrimoireData>> GRIMOIRE_DATA = ATTACHMENTS.register("grimoire", () -> AttachmentType
            .builder(() -> GrimoireData.EMPTY)
            .serialize(GrimoireData.CODEC)
            .copyOnDeath()
            // everyone who sees the player draws their floating grimoire
            .sync(GrimoireData.STREAM_CODEC)
            .build());

    public static final DeferredBlock<GrimoireAltarBlock> ALTAR = BLOCKS.registerBlock("grimoire_altar", GrimoireAltarBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.5f, 1200f).sound(SoundType.STONE).noOcclusion()
                    .lightLevel(state -> 10).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> ALTAR_ITEM = ITEMS.registerSimpleBlockItem(ALTAR, new Item.Properties());
    public static final Supplier<BlockEntityType<GrimoireAltarBlockEntity>> ALTAR_BLOCK_ENTITY = BLOCK_ENTITIES.register("grimoire_altar",
            () -> BlockEntityType.Builder.of(GrimoireAltarBlockEntity::new, ALTAR.get()).build(null));

    public static final Supplier<EntityType<Priest>> PRIEST = ENTITIES.register("priest", () -> EntityType.Builder
            .<Priest>of(Priest::new, MobCategory.MISC).sized(0.6f, 1.95f).clientTrackingRange(10).build("priest"));

    private GrimoireModule() {}

    public static void register(IEventBus modBus, ModContainer container) {
        CoreRegistries.register(modBus, GrimoireRegistries.ELEMENTS);
        GrimoireRegistries.ELEMENT_ENTRIES.register(modBus);
        ATTACHMENTS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        ENTITIES.register(modBus);
        GrimoireGameRules.init();
        Configs.registerServer(container, "grimoire", GrimoireConfig.SPEC);

        modBus.addListener(RegisterPayloadHandlersEvent.class, GrimoireModule::registerPayloads);
        modBus.addListener(EntityAttributeCreationEvent.class, event -> event.put(PRIEST.get(), Villager.createAttributes().build()));
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, event -> {
            if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) event.accept(ALTAR_ITEM);
        });

        NeoForge.EVENT_BUS.addListener(LivingDamageEvent.Post.class, GrimoireEvents::onDamage);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, GrimoireEvents::onLogin);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedOutEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                GrimoireActivity.forget(player);
                CeremonyService.forget(player);
            }
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> {
            GkCommands.register(event.getDispatcher(), GrimoireCommand.grimoire());
            GkCommands.register(event.getDispatcher(), GrimoireCommand.ceremony());
            GkCommands.register(event.getDispatcher(), GrimoireCommand.calling());
            GkCommands.register(event.getDispatcher(), GrimoireCommand.anim());
        });
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        Network.registrar(event)
                .playToClient(GrimoireActivityPayload.TYPE, GrimoireActivityPayload.STREAM_CODEC, (payload, context) -> GrimoireActivityPayload.CLIENT.accept(payload))
                .playToClient(CeremonyPayload.TYPE, CeremonyPayload.STREAM_CODEC, (payload, context) -> CeremonyPayload.CLIENT.accept(payload))
                .playToClient(ElementChoicePayload.TYPE, ElementChoicePayload.STREAM_CODEC, (payload, context) -> ElementChoicePayload.CLIENT.accept(payload))
                .playToClient(PlayerAnimationPayload.TYPE, PlayerAnimationPayload.STREAM_CODEC, (payload, context) -> PlayerAnimationPayload.CLIENT.accept(payload))
                .playToServer(ChooseElementPayload.TYPE, ChooseElementPayload.STREAM_CODEC, (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) CeremonyService.choose(player, payload.element());
                });
    }
}
