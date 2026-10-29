package com.navesdev.grimoireknights.ability;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.ability.attachment.BurnData;
import com.navesdev.grimoireknights.ability.attachment.CombatData;
import com.navesdev.grimoireknights.ability.command.AbilityCommand;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.ability.entity.FireballProjectile;
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
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Abilities on Z/X/C, mana, cooldowns and the fire burn. */
public final class AbilityModule {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, GrimoireKnights.MODID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, GrimoireKnights.MODID);

    public static final ResourceKey<DamageType> FIREBALL_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, Ids.id("fireball"));

    public static final Supplier<AttachmentType<CombatData>> COMBAT_DATA = ATTACHMENTS.register("combat", () -> AttachmentType
            .builder(() -> CombatData.EMPTY)
            .serialize(CombatData.CODEC)
            .copyOnDeath()
            .sync((holder, to) -> holder == to, CombatData.STREAM_CODEC)
            .build());
    public static final Supplier<AttachmentType<BurnData>> BURN = ATTACHMENTS.register("burn", () -> AttachmentType
            .builder(() -> new BurnData(0, 0))
            .build());

    public static final Supplier<EntityType<FireballProjectile>> FIREBALL = ENTITIES.register("fireball", () -> EntityType.Builder
            .<FireballProjectile>of(FireballProjectile::new, MobCategory.MISC).sized(0.5f, 0.5f)
            .clientTrackingRange(8).updateInterval(1).build("fireball"));

    private AbilityModule() {}

    public static void register(IEventBus modBus, ModContainer container) {
        CoreRegistries.register(modBus, AbilityRegistries.ABILITIES);
        AbilityRegistries.ABILITY_ENTRIES.register(modBus);
        ATTACHMENTS.register(modBus);
        ENTITIES.register(modBus);
        Configs.registerServer(container, "ability", AbilityConfig.SPEC);

        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> Network.registrar(event)
                .playToServer(CastAbilityPayload.TYPE, CastAbilityPayload.STREAM_CODEC, (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) AbilityService.tryCast(player, payload.slot());
                }));

        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, AbilityEvents::onLogin);
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, AbilityEvents::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(EntityTickEvent.Post.class, AbilityEvents::onEntityTick);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> {
            GkCommands.register(event.getDispatcher(), AbilityCommand.status());
            GkCommands.register(event.getDispatcher(), AbilityCommand.cooldown());
        });
    }
}
