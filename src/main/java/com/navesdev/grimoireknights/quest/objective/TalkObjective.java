package com.navesdev.grimoireknights.quest.objective;

import com.mojang.serialization.MapCodec;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.quest.Objective;
import com.navesdev.grimoireknights.quest.ObjectiveType;
import com.navesdev.grimoireknights.quest.QuestModule;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

/** {@code talk}: talk to an NPC of this entity type. */
public record TalkObjective(EntityType<?> npc) implements Objective {
    public static final MapCodec<TalkObjective> CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("npc").xmap(TalkObjective::new, TalkObjective::npc);

    @Override
    public ObjectiveType<?> type() {
        return QuestModule.TALK.get();
    }

    @Override
    public int required() {
        return 1;
    }

    @Override
    public int onTalk(ServerPlayer player, EntityType<?> talkedTo) {
        return talkedTo == npc ? 1 : 0;
    }

    @Override
    public Component describe() {
        return Component.translatable(Ids.key("objective", "talk"), npc.getDescription());
    }
}
