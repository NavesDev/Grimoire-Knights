package com.navesdev.grimoireknights.ability.entity;

import com.navesdev.grimoireknights.ability.HeldService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Base of the spell projectiles: straight flight with no gravity, a range in ticks, an upgrade level synced to the
 * renderer (which hides upgrade parts), never hitting its caster or a target someone already holds. Not saved: spells don't outlive a reload.
 */
public abstract class SpellProjectile extends ThrowableProjectile implements GeoEntity {
    private static final EntityDataAccessor<Integer> UPGRADE = SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.FLOAT);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected int maxTicks = 40;

    protected SpellProjectile(EntityType<? extends SpellProjectile> type, Level level) {
        super(type, level);
    }

    protected SpellProjectile(EntityType<? extends SpellProjectile> type, LivingEntity owner, Level level) {
        super(type, owner, level);
    }

    /** Places it at the caster's eyes and launches it along {@code direction}, flying {@code range} blocks. */
    public SpellProjectile launch(LivingEntity caster, Vec3 direction, double speed, double range) {
        setPos(caster.getEyePosition().add(direction.scale(0.8)).subtract(0, 0.2, 0));
        shoot(direction.x, direction.y, direction.z, (float) speed, 0);
        maxTicks = (int) Math.ceil(range / speed);
        return this;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(UPGRADE, 0);
        builder.define(SCALE, 1f);
    }

    public int upgrade() {
        return entityData.get(UPGRADE);
    }

    public SpellProjectile withUpgrade(int level) {
        entityData.set(UPGRADE, level);
        return this;
    }

    public float scale() {
        return entityData.get(SCALE);
    }

    public SpellProjectile withScale(float value) {
        entityData.set(SCALE, value);
        return this;
    }

    @Override
    protected double getDefaultGravity() {
        return 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount >= maxTicks) outOfRange();
    }

    /** Range spent with nothing hit: vanish by default. */
    protected void outOfRange() {
        discard();
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && target != getOwner() && !(target instanceof LivingEntity living && HeldService.held(living));
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
