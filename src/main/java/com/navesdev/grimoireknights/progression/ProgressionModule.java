package com.navesdev.grimoireknights.progression;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.command.GkCommands;
import com.navesdev.grimoireknights.core.config.Configs;
import com.navesdev.grimoireknights.core.network.Network;
import com.navesdev.grimoireknights.progression.attachment.MasteryData;
import com.navesdev.grimoireknights.progression.attachment.RankData;
import com.navesdev.grimoireknights.progression.attachment.SkillTreeData;
import com.navesdev.grimoireknights.progression.command.ProgressionCommand;
import com.navesdev.grimoireknights.progression.config.ProgressionConfig;
import com.navesdev.grimoireknights.progression.event.ProgressionEvents;
import com.navesdev.grimoireknights.progression.network.BuyNodePayload;
import com.navesdev.grimoireknights.progression.network.DashCooldownPayload;
import com.navesdev.grimoireknights.progression.network.DashPayload;
import com.navesdev.grimoireknights.progression.quest.MasteryReward;
import com.navesdev.grimoireknights.progression.quest.RankUpReward;
import com.navesdev.grimoireknights.quest.QuestRegistries;
import com.navesdev.grimoireknights.quest.RewardType;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

/** Masteries, skill tree, ranks and the dash. */
public final class ProgressionModule {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, GrimoireKnights.MODID);
    private static final DeferredRegister<RewardType<?>> REWARDS = DeferredRegister.create(QuestRegistries.REWARD_TYPE, GrimoireKnights.MODID);

    public static final Supplier<AttachmentType<MasteryData>> MASTERY_DATA = ATTACHMENTS.register("mastery", () -> AttachmentType
            .builder(() -> MasteryData.EMPTY)
            .serialize(MasteryData.CODEC)
            .copyOnDeath()
            .sync((holder, to) -> holder == to, MasteryData.STREAM_CODEC)
            .build());
    public static final Supplier<AttachmentType<SkillTreeData>> SKILL_TREE_DATA = ATTACHMENTS.register("skill_tree", () -> AttachmentType
            .builder(() -> SkillTreeData.EMPTY)
            .serialize(SkillTreeData.CODEC)
            .copyOnDeath()
            .sync((holder, to) -> holder == to, SkillTreeData.STREAM_CODEC)
            .build());
    public static final Supplier<AttachmentType<RankData>> RANK_DATA = ATTACHMENTS.register("rank", () -> AttachmentType
            .builder(() -> RankData.EMPTY)
            .serialize(RankData.CODEC)
            .copyOnDeath()
            .sync((holder, to) -> holder == to, RankData.STREAM_CODEC)
            .build());

    public static final Supplier<RewardType<RankUpReward>> RANK_UP = REWARDS.register("rank_up", () -> new RewardType<>(RankUpReward.CODEC));
    public static final Supplier<RewardType<MasteryReward>> MASTERY_REWARD = REWARDS.register("mastery", () -> new RewardType<>(MasteryReward.CODEC));

    private ProgressionModule() {}

    public static void register(IEventBus modBus, ModContainer container) {
        ATTACHMENTS.register(modBus);
        REWARDS.register(modBus);
        Configs.registerServer(container, "progression", ProgressionConfig.SPEC);
        modBus.addListener(DataPackRegistryEvent.NewRegistry.class,
                event -> event.dataPackRegistry(ProgressionRegistries.SKILL_NODE, SkillNode.CODEC, SkillNode.CODEC));
        modBus.addListener(RegisterDataMapTypesEvent.class, event -> event.register(ProgressionRegistries.MASTERY));
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> Network.registrar(event)
                .playToServer(DashPayload.TYPE, DashPayload.STREAM_CODEC, (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) DashService.tryDash(player, payload.x(), payload.z());
                })
                .playToServer(BuyNodePayload.TYPE, BuyNodePayload.STREAM_CODEC, (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) SkillTreeService.buy(player, payload.node());
                })
                .playToClient(DashCooldownPayload.TYPE, DashCooldownPayload.STREAM_CODEC, (payload, context) -> DashCooldownPayload.CLIENT.accept(payload)));

        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, ProgressionEvents::onIncoming);
        NeoForge.EVENT_BUS.addListener(LivingDamageEvent.Pre.class, ProgressionEvents::onDamagePre);
        NeoForge.EVENT_BUS.addListener(LivingDamageEvent.Post.class, ProgressionEvents::onDamage);
        NeoForge.EVENT_BUS.addListener(LivingDeathEvent.class, ProgressionEvents::onDeath);
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, ProgressionEvents::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedOutEvent.class, ProgressionEvents::onLogout);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> {
            GkCommands.register(event.getDispatcher(), ProgressionCommand.mastery());
            GkCommands.register(event.getDispatcher(), ProgressionCommand.tree());
            GkCommands.register(event.getDispatcher(), ProgressionCommand.rank());
        });
    }
}
