package com.navesdev.grimoireknights.quest;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.command.GkCommands;
import com.navesdev.grimoireknights.core.registry.CoreRegistries;
import com.navesdev.grimoireknights.quest.attachment.QuestData;
import com.navesdev.grimoireknights.quest.command.QuestCommand;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Generic quest engine: datapack quests, objective and reward types, per-player progress and quest events. */
public final class QuestModule {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, GrimoireKnights.MODID);

    public static final Supplier<AttachmentType<QuestData>> QUEST_DATA = ATTACHMENTS.register("quest", () -> AttachmentType
            .builder(() -> QuestData.EMPTY)
            .serialize(QuestData.CODEC)
            .copyOnDeath()
            .sync((holder, to) -> holder == to, QuestData.STREAM_CODEC)
            .build());

    private QuestModule() {}

    public static void register(IEventBus modBus, ModContainer container) {
        CoreRegistries.register(modBus, QuestRegistries.OBJECTIVE_TYPES, QuestRegistries.REWARD_TYPES);
        ATTACHMENTS.register(modBus);
        modBus.addListener(DataPackRegistryEvent.NewRegistry.class, event -> event.dataPackRegistry(QuestRegistries.QUEST, Quest.CODEC));
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> GkCommands.register(event.getDispatcher(), QuestCommand.branch()));
    }
}
