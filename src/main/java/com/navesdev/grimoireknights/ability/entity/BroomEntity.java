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
import net.minecraft.util.Mth;
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
 * slowly with no fall damage and drops its rider on landing. It vanishes without a rider. It banks into strafes with
 * its rider's lean.
 */
public class BroomEntity extends Entity implements GeoEntity {
    private static final RawAnimation HOVER = RawAnimation.begin().thenLoop("animation.broom.hover");
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("animation.broom.fly");
    private static final EntityDataAccessor<Boolean> GLIDING = SynchedEntityData.defineId(BroomEntity.class, EntityDataSerializers.BOOLEAN);
    private static final double MOVING = 0.01;
    /** Degrees it banks into a strafe, as far as its rider leans in broom_left and broom_right (player_ride.js). */
    private static final float BANK = 16;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private float roll;
    private float rollO;

    /** Where the broom moves relative to its rider's facing; picks the rider's pose and the broom's bank. */
    public enum Lean { HOVER, FORWARD, BACKWARD, LEFT, RIGHT }

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
        if (level().isClientSide) bank(rider);
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

    public Lean lean(float riderYaw) {
        Vec3 motion = getDeltaMovement();
        if (motion.horizontalDistanceSqr() < MOVING) return Lean.HOVER;
        Vec3 facing = Vec3.directionFromRotation(0, riderYaw);
        double forward = motion.x * facing.x + motion.z * facing.z;
        double side = motion.x * -facing.z + motion.z * facing.x;
        if (Math.abs(side) > Math.abs(forward)) return side > 0 ? Lean.RIGHT : Lean.LEFT;
        return forward >= 0 ? Lean.FORWARD : Lean.BACKWARD;
    }

    /** Client: eases into the bank of the current strafe, toward the right when positive. */
    private void bank(@Nullable LivingEntity rider) {
        rollO = roll;
        Lean lean = rider == null ? Lean.HOVER : lean(rider.getYRot());
        float target = lean == Lean.RIGHT ? BANK : lean == Lean.LEFT ? -BANK : 0;
        roll += (target - roll) * 0.25f;
    }

    public float roll(float partialTick) {
        return Mth.lerp(partialTick, rollO, roll);
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
                state -> state.setAndContinue(getDeltaMovement().horizontalDistanceSqr() > MOVING ? FLY : HOVER)));
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
