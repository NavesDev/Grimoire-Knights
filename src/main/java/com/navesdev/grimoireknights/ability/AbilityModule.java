package com.navesdev.grimoireknights.ability;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.ability.attachment.BurnData;
import com.navesdev.grimoireknights.ability.attachment.CombatData;
import com.navesdev.grimoireknights.ability.command.AbilityCommand;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.ability.entity.BroomEntity;
import com.navesdev.grimoireknights.ability.entity.FireballProjectile;
import com.navesdev.grimoireknights.ability.item.BroomItem;
import com.navesdev.grimoireknights.core.registry.CoreTabs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import com.navesdev.grimoireknights.ability.entity.FlameFistEntity;
import com.navesdev.grimoireknights.ability.entity.TornadoEntity;
import com.navesdev.grimoireknights.ability.entity.WindArrowEntity;
import com.navesdev.grimoireknights.ability.entity.WindBladeEntity;
import com.navesdev.grimoireknights.ability.entity.WindBowEntity;
import com.navesdev.grimoireknights.ability.network.TailwindPayload;
import com.navesdev.grimoireknights.ability.event.AbilityEvents;
import com.navesdev.grimoireknights.ability.network.CastAbilityPayload;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.command.GkCommands;
import com.navesdev.grimoireknights.core.config.Configs;
import com.navesdev.grimoireknights.core.network.Network;
import com.navesdev.grimoireknights.core.registry.CoreRegistries;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import com.navesdev.grimoireknights.progression.event.DashEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Abilities on Z/X/C, mana, cooldowns, the fire burn, held targets and the passives. */
public final class AbilityModule {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, GrimoireKnights.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GrimoireKnights.MODID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, GrimoireKnights.MODID);

    public static final ResourceKey<DamageType> FIREBALL_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, Ids.id("fireball"));

    public static final Supplier<AttachmentType<CombatData>> COMBAT_DATA = ATTACHMENTS.register("combat", () -> AttachmentType
            .builder(() -> CombatData.EMPTY)
            .serialize(CombatData.CODEC)
            .copyOnDeath()
            .sync((holder, to) -> holder == to, CombatData.STREAM_CODEC)
            .build());
    public static final Supplier<AttachmentType<BurnData>> BURN = ATTACHMENTS.register("burn", () -> AttachmentType
            .builder(() -> new BurnData(0, java.util.Optional.empty()))
            .build());

    public static final Supplier<EntityType<FireballProjectile>> FIREBALL = ENTITIES.register("fireball", () -> EntityType.Builder
            .<FireballProjectile>of(FireballProjectile::new, MobCategory.MISC).sized(0.5f, 0.5f)
            .clientTrackingRange(8).updateInterval(1).build("fireball"));

    public static final Supplier<EntityType<FlameFistEntity>> FLAME_FIST = ENTITIES.register("flame_fist", () -> EntityType.Builder
            .<FlameFistEntity>of(FlameFistEntity::new, MobCategory.MISC).sized(1.0f, 1.0f)
            .clientTrackingRange(8).updateInterval(1).build("flame_fist"));
    public static final Supplier<EntityType<WindBladeEntity>> WIND_BLADE = ENTITIES.register("wind_blade", () -> EntityType.Builder
            .<WindBladeEntity>of(WindBladeEntity::new, MobCategory.MISC).sized(1.2f, 0.5f)
            .clientTrackingRange(8).updateInterval(1).build("wind_blade"));
    public static final Supplier<EntityType<TornadoEntity>> TORNADO = ENTITIES.register("tornado", () -> EntityType.Builder
            .<TornadoEntity>of(TornadoEntity::new, MobCategory.MISC).sized(1.2f, 1.2f)
            .clientTrackingRange(8).updateInterval(1).build("tornado"));
    public static final Supplier<EntityType<WindArrowEntity>> WIND_ARROW = ENTITIES.register("wind_arrow", () -> EntityType.Builder
            .<WindArrowEntity>of(WindArrowEntity::new, MobCategory.MISC).sized(0.5f, 0.5f)
            .clientTrackingRange(8).updateInterval(1).build("wind_arrow"));
    public static final Supplier<EntityType<WindBowEntity>> WIND_BOW = ENTITIES.register("wind_bow", () -> EntityType.Builder
            .<WindBowEntity>of(WindBowEntity::new, MobCategory.MISC).sized(0.5f, 0.5f)
            .clientTrackingRange(8).updateInterval(1).build("wind_bow"));

    public static final Supplier<EntityType<BroomEntity>> BROOM = ENTITIES.register("broom", () -> EntityType.Builder
            .<BroomEntity>of(BroomEntity::new, MobCategory.MISC).sized(0.8f, 0.3f)
            // the rider stands on the handle (1.5 pixels): lifted past the player's 0.6 sitting offset
            .passengerAttachments(0.6f + 1.5f / 16)
            .clientTrackingRange(10).updateInterval(1).build("broom"));

    public static final DeferredItem<Item> MANA_DUST = ITEMS.registerSimpleItem("mana_dust", new Item.Properties());
    public static final DeferredItem<BroomItem> BROOM_ITEM = ITEMS.registerItem("broom", BroomItem::new, new Item.Properties().stacksTo(1));

    private AbilityModule() {}

    public static void register(IEventBus modBus, ModContainer container) {
        CoreRegistries.register(modBus, AbilityRegistries.ABILITIES);
        AbilityRegistries.ABILITY_ENTRIES.register(modBus);
        ATTACHMENTS.register(modBus);
        ENTITIES.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, event -> {
            if (event.getTabKey() == CoreTabs.MAIN) {
                event.accept(MANA_DUST);
                event.accept(BROOM_ITEM);
            }
        });
        Configs.registerServer(container, "ability", AbilityConfig.SPEC);

        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> Network.registrar(event)
                .playToServer(CastAbilityPayload.TYPE, CastAbilityPayload.STREAM_CODEC, (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) AbilityService.tryCast(player, payload.slot());
                })
                .playToClient(TailwindPayload.TYPE, TailwindPayload.STREAM_CODEC, (payload, context) -> TailwindPayload.CLIENT.accept(payload)));

        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, AbilityEvents::onLogin);
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, AbilityEvents::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(EntityTickEvent.Post.class, AbilityEvents::onEntityTick);
        NeoForge.EVENT_BUS.addListener(LevelTickEvent.Post.class, AbilityEvents::onLevelTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, LivingDeathEvent.class, AbilityEvents::onDeath);
        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, AbilityEvents::onIncoming);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedOutEvent.class, AbilityEvents::onLogout);
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, event -> Delayed.clear());
        NeoForge.EVENT_BUS.addListener(DashEvent.class, event -> {
            if (HeldService.held(event.getEntity())) event.setCanceled(true);
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> {
            GkCommands.register(event.getDispatcher(), AbilityCommand.mana());
            GkCommands.register(event.getDispatcher(), AbilityCommand.cooldown());
        });
    }
}
