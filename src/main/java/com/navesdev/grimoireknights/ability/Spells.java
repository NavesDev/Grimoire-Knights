package com.navesdev.grimoireknights.ability;

import com.navesdev.grimoireknights.core.Ids;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Damage sources and area lookups shared by the spells. */
public final class Spells {
    public static final ResourceKey<DamageType> FIRE = ResourceKey.create(Registries.DAMAGE_TYPE, Ids.id("fire_spell"));
    public static final ResourceKey<DamageType> WIND = ResourceKey.create(Registries.DAMAGE_TYPE, Ids.id("wind_spell"));

    private Spells() {}

    public static DamageSource source(ServerLevel level, ResourceKey<DamageType> type, Entity direct, Entity owner) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type), direct, owner);
    }

    /** Living entities whose box is within {@code radius} of {@code center}, never {@code owner}. */
    public static List<LivingEntity> around(ServerLevel level, Vec3 center, double radius, Entity owner, Predicate<LivingEntity> also) {
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius), target -> target != owner && target.isAlive()
                && !target.isSpectator() && target.getBoundingBox().distanceToSqr(center) <= radius * radius && also.test(target));
    }
}
