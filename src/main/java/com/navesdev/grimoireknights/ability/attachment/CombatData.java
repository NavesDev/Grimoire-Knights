package com.navesdev.grimoireknights.ability.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * Current mana, the Mana stat (until progression owns it) and cooldowns as the game time each ability is ready again.
 * {@code mana} below zero means not set yet: it starts full. Immutable: change with {@code setData}.
 */
public record CombatData(double mana, int manaStat, Map<ResourceLocation, Long> readyAt) {
    public static final CombatData EMPTY = new CombatData(-1, 0, Map.of());

    public static final Codec<CombatData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("mana", -1.0).forGetter(CombatData::mana),
            Codec.INT.optionalFieldOf("mana_stat", 0).forGetter(CombatData::manaStat),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.LONG).optionalFieldOf("ready_at", Map.of()).forGetter(CombatData::readyAt)
    ).apply(instance, CombatData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CombatData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, CombatData::mana,
            ByteBufCodecs.VAR_INT, CombatData::manaStat,
            ByteBufCodecs.<RegistryFriendlyByteBuf, ResourceLocation, Long, Map<ResourceLocation, Long>>map(
                    HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_LONG),
            CombatData::readyAt,
            CombatData::new);

    public CombatData {
        readyAt = Map.copyOf(readyAt);
    }

    public boolean manaSet() {
        return mana >= 0;
    }

    public long cooldownLeft(ResourceLocation ability, long now) {
        return Math.max(0, readyAt.getOrDefault(ability, now) - now);
    }

    public CombatData withMana(double value) {
        return new CombatData(Math.max(0, value), manaStat, readyAt);
    }

    public CombatData withManaStat(int stat, double mana) {
        return new CombatData(Math.max(0, mana), stat, readyAt);
    }

    public CombatData withCooldown(ResourceLocation ability, long readyAtTick) {
        Map<ResourceLocation, Long> next = new HashMap<>(readyAt);
        next.put(ability, readyAtTick);
        return new CombatData(mana, manaStat, next);
    }

    public CombatData withoutCooldowns() {
        return new CombatData(mana, manaStat, Map.of());
    }
}
