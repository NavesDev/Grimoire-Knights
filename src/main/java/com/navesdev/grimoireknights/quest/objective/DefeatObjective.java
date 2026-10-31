package com.navesdev.grimoireknights.quest.objective;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.quest.Objective;
import com.navesdev.grimoireknights.quest.ObjectiveType;
import com.navesdev.grimoireknights.quest.QuestModule;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/** {@code defeat}: kill {@code count} entities of a type or a tag. {@code name} is a translation key for the target, when the tag needs one. */
public record DefeatObjective(Optional<EntityType<?>> entity, Optional<TagKey<EntityType<?>>> tag, int count, Optional<String> name) implements Objective {
    public static final MapCodec<DefeatObjective> CODEC = RecordCodecBuilder.<DefeatObjective>mapCodec(instance -> instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().optionalFieldOf("entity").forGetter(DefeatObjective::entity),
            TagKey.codec(Registries.ENTITY_TYPE).optionalFieldOf("tag").forGetter(DefeatObjective::tag),
            Codec.intRange(1, 10_000).optionalFieldOf("count", 1).forGetter(DefeatObjective::count),
            Codec.STRING.optionalFieldOf("name").forGetter(DefeatObjective::name)
    ).apply(instance, DefeatObjective::new)).validate(objective -> objective.entity().isPresent() != objective.tag().isPresent()
            ? DataResult.success(objective) : DataResult.error(() -> "defeat needs exactly one of entity or tag"));

    @Override
    public ObjectiveType<?> type() {
        return QuestModule.DEFEAT.get();
    }

    @Override
    public int required() {
        return count;
    }

    @Override
    public int onKill(ServerPlayer player, LivingEntity killed) {
        boolean matches = entity.map(type -> killed.getType() == type).orElse(false) || tag.map(t -> killed.getType().is(t)).orElse(false);
        return matches ? 1 : 0;
    }

    @Override
    public Component describe() {
        Component target = name.<Component>map(Component::translatable)
                .or(() -> entity.map(EntityType::getDescription))
                .orElseGet(() -> Component.literal(tag.map(t -> t.location().toString()).orElse("?")));
        return Component.translatable(Ids.key("objective", "defeat"), target, count);
    }
}
