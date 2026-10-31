package com.navesdev.grimoireknights.ability.entity;

import com.navesdev.grimoireknights.ability.AbilityModule;
import com.navesdev.grimoireknights.ability.AbilityService;
import com.navesdev.grimoireknights.ability.BroomService;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * A ridden broom, standing like a hoverboard. The rider steers with the movement keys toward where they look; the
 * client of the rider moves it (vanilla vehicle rule), the server spends mana every second. Out of mana it glides down
 * slowly with no fall damage and drops its rider on landing. It vanishes without a rider.
 */
public class BroomEntity extends Entity implements GeoEntity {
    private static final RawAnimation HOVER = RawAnimation.begin().thenLoop("animation.broom.hover");
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("animation.broom.fly");
    private static final EntityDataAccessor<Boolean> GLIDING = SynchedEntityData.defineId(BroomEntity.class, EntityDataSerializers.BOOLEAN);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BroomEntity(EntityType<? extends BroomEntity> type, Level level) {
        super(type, level);
    }

    public BroomEntity(Level level, Player rider) {
        this(AbilityModule.BROOM.get(), level);
        setPos(rider.position());
        setYRot(rider.getYRot());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(GLIDING, false);
    }

    public boolean gliding() {
        return entityData.get(GLIDING);
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity rider = getControllingPassenger();
        if (!level().isClientSide) {
            if (rider == null) {
                discard();
                return;
            }
            if (rider instanceof ServerPlayer player && tickCount % 20 == 0) {
                entityData.set(GLIDING, !BroomService.payFlight(player));
            }
            if (gliding() && onGround()) {
                rider.stopRiding();
                return;
            }
        }
        if (rider != null) rider.fallDistance = 0;
        fallDistance = 0;
        if (rider instanceof Player player && isControlledByLocalInstance()) steer(player);
        else if (rider == null) setDeltaMovement(getDeltaMovement().scale(0.8));
    }

    /** Forward and back along the look (up and down with it), strafing sideways; gliding only sinks forward slowly. */
    private void steer(Player rider) {
        double speed = AbilityConfig.BROOM_SPEED.get();
        Vec3 look = rider.getLookAngle();
        Vec3 side = new Vec3(-look.z, 0, look.x).normalize();
        Vec3 wanted = look.scale(rider.zza > 0 ? speed : rider.zza < 0 ? -speed * 0.5 : 0).add(side.scale(-rider.xxa * speed * 0.6));
        if (gliding()) wanted = new Vec3(wanted.x * 0.4, -AbilityConfig.BROOM_GLIDE_FALL.get(), wanted.z * 0.4);
        setDeltaMovement(getDeltaMovement().scale(0.6).add(wanted.scale(0.4)));
        setYRot(rider.getYRot());
        move(MoverType.SELF, getDeltaMovement());
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        return getFirstPassenger() instanceof Player player ? player : null;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty() && passenger instanceof Player;
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!level().isClientSide) discard();
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4,
                state -> state.setAndContinue(getDeltaMovement().horizontalDistanceSqr() > 0.01 ? FLY : HOVER)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /** Mana a second of flight costs this rider, after Mana Control; the HUD shows it. */
    public static double manaPerSecond(Player rider) {
        return AbilityService.cost(rider, AbilityConfig.BROOM_MANA_PER_SECOND.get());
    }
}
