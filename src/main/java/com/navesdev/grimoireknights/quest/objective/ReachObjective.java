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
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;

/** {@code reach}: stand inside a structure, or within {@code radius} blocks of a position. {@code name} labels the place. */
public record ReachObjective(Optional<ResourceKey<Structure>> structure, Optional<BlockPos> pos, double radius, Optional<String> name) implements Objective {
    public static final MapCodec<ReachObjective> CODEC = RecordCodecBuilder.<ReachObjective>mapCodec(instance -> instance.group(
            ResourceKey.codec(Registries.STRUCTURE).optionalFieldOf("structure").forGetter(ReachObjective::structure),
            BlockPos.CODEC.optionalFieldOf("pos").forGetter(ReachObjective::pos),
            Codec.doubleRange(1, 1_000).optionalFieldOf("radius", 8.0).forGetter(ReachObjective::radius),
            Codec.STRING.optionalFieldOf("name").forGetter(ReachObjective::name)
    ).apply(instance, ReachObjective::new)).validate(objective -> objective.structure().isPresent() != objective.pos().isPresent()
            ? DataResult.success(objective) : DataResult.error(() -> "reach needs exactly one of structure or pos"));

    @Override
    public ObjectiveType<?> type() {
        return QuestModule.REACH.get();
    }

    @Override
    public int required() {
        return 1;
    }

    @Override
    public boolean reached(ServerPlayer player) {
        if (pos.isPresent()) return player.blockPosition().distSqr(pos.get()) <= radius * radius;
        Optional<Structure> target = structure.flatMap(key -> player.serverLevel().registryAccess().registryOrThrow(Registries.STRUCTURE).getOptional(key));
        return target.isPresent() && player.serverLevel().structureManager().getStructureWithPieceAt(player.blockPosition(), target.get()).isValid();
    }

    @Override
    public Component describe() {
        Component place = name.<Component>map(Component::translatable)
                .orElseGet(() -> Component.literal(structure.map(key -> key.location().getPath())
                        .orElseGet(() -> pos.map(p -> p.toShortString()).orElse("?"))));
        return Component.translatable(Ids.key("objective", "reach"), place);
    }
}
