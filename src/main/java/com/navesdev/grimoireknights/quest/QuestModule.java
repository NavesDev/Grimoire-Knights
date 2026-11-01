package com.navesdev.grimoireknights.quest;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.command.GkCommands;
import com.navesdev.grimoireknights.core.network.Network;
import com.navesdev.grimoireknights.core.registry.CoreRegistries;
import com.navesdev.grimoireknights.quest.attachment.QuestData;
import com.navesdev.grimoireknights.quest.command.QuestCommand;
import com.navesdev.grimoireknights.quest.network.DialoguePayload;
import com.navesdev.grimoireknights.quest.network.QuestAnswerPayload;
import com.navesdev.grimoireknights.quest.objective.DefeatObjective;
import com.navesdev.grimoireknights.quest.objective.ReachObjective;
import com.navesdev.grimoireknights.quest.objective.TalkObjective;
import com.navesdev.grimoireknights.quest.reward.ItemReward;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Generic quest engine: datapack quests, objective and reward types, per-player progress, NPC dialogue and quest events. */
public final class QuestModule {
    private static final int REACH_INTERVAL = 20;
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, GrimoireKnights.MODID);
    private static final DeferredRegister<ObjectiveType<?>> OBJECTIVES = DeferredRegister.create(QuestRegistries.OBJECTIVE_TYPE, GrimoireKnights.MODID);
    private static final DeferredRegister<RewardType<?>> REWARDS = DeferredRegister.create(QuestRegistries.REWARD_TYPE, GrimoireKnights.MODID);

    public static final Supplier<AttachmentType<QuestData>> QUEST_DATA = ATTACHMENTS.register("quest", () -> AttachmentType
            .builder(() -> QuestData.EMPTY)
            .serialize(QuestData.CODEC)
            .copyOnDeath()
            .sync((holder, to) -> holder == to, QuestData.STREAM_CODEC)
            .build());

    public static final Supplier<ObjectiveType<DefeatObjective>> DEFEAT = OBJECTIVES.register("defeat", () -> new ObjectiveType<>(DefeatObjective.CODEC));
    public static final Supplier<ObjectiveType<ReachObjective>> REACH = OBJECTIVES.register("reach", () -> new ObjectiveType<>(ReachObjective.CODEC));
    public static final Supplier<ObjectiveType<TalkObjective>> TALK = OBJECTIVES.register("talk", () -> new ObjectiveType<>(TalkObjective.CODEC));
    public static final Supplier<RewardType<ItemReward>> ITEM = REWARDS.register("item", () -> new RewardType<>(ItemReward.CODEC));

    private QuestModule() {}

    public static void register(IEventBus modBus, ModContainer container) {
        CoreRegistries.register(modBus, QuestRegistries.OBJECTIVE_TYPES, QuestRegistries.REWARD_TYPES);
        ATTACHMENTS.register(modBus);
        OBJECTIVES.register(modBus);
        REWARDS.register(modBus);
        // synced so the journal, tracker and dialogue can show objectives and rewards
        modBus.addListener(DataPackRegistryEvent.NewRegistry.class, event -> event.dataPackRegistry(QuestRegistries.QUEST, Quest.CODEC, Quest.CODEC));
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> Network.registrar(event)
                .playToClient(DialoguePayload.TYPE, DialoguePayload.STREAM_CODEC, (payload, context) -> DialoguePayload.CLIENT.accept(payload))
                .playToServer(QuestAnswerPayload.TYPE, QuestAnswerPayload.STREAM_CODEC, (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) QuestService.answer(player, payload.quest(), payload.accept());
                }));
        NeoForge.EVENT_BUS.addListener(LivingDeathEvent.class, event -> {
            if (event.getSource().getEntity() instanceof ServerPlayer player && player != event.getEntity()) {
                QuestService.advance(player, objective -> objective.onKill(player, event.getEntity()));
            }
        });
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player && player.tickCount % REACH_INTERVAL == 0 && !QuestService.data(player).active().isEmpty()) {
                QuestService.advance(player, objective -> objective.reached(player) ? objective.required() : 0);
            }
        });
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedOutEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) QuestService.forget(player);
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> GkCommands.register(event.getDispatcher(), QuestCommand.branch()));
    }
}
