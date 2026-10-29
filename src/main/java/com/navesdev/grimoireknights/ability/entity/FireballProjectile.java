package com.navesdev.grimoireknights.ability.entity;

import com.navesdev.grimoireknights.ability.AbilityModule;
import com.navesdev.grimoireknights.ability.BurnService;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** The fireball: flies straight (no gravity), hurts what it hits and stacks fire's burn. */
public class FireballProjectile extends ThrowableProjectile implements GeoEntity {
    private static final RawAnimation SPIN = RawAnimation.begin().thenLoop("animation.fireball.spin");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public FireballProjectile(EntityType<? extends FireballProjectile> type, Level level) {
        super(type, level);
    }

    public FireballProjectile(Level level, LivingEntity owner) {
        super(AbilityModule.FIREBALL.get(), owner, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected double getDefaultGravity() {
        return 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            Vec3 motion = getDeltaMovement();
            for (int i = 0; i < 2; i++) {
                level().addParticle(i == 0 ? ParticleTypes.FLAME : ParticleTypes.SMOKE,
                        getX() - motion.x * 0.5 + (random.nextDouble() - 0.5) * 0.3,
                        getY() + 0.25 - motion.y * 0.5 + (random.nextDouble() - 0.5) * 0.3,
                        getZ() - motion.z * 0.5 + (random.nextDouble() - 0.5) * 0.3, 0, 0.01, 0);
            }
        } else if (tickCount > AbilityConfig.FIREBALL_LIFETIME_TICKS.get()) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide) return;
        Entity target = result.getEntity();
        DamageSource source = new DamageSource(level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                .getHolderOrThrow(AbilityModule.FIREBALL_DAMAGE), this, getOwner());
        if (target.hurt(source, AbilityConfig.FIREBALL_DAMAGE.get().floatValue()) && target instanceof LivingEntity living) {
            BurnService.addStack(living);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.25, getZ(), 24, 0.3, 0.3, 0.3, 0.05);
            server.sendParticles(ParticleTypes.LAVA, getX(), getY() + 0.25, getZ(), 4, 0.2, 0.2, 0.2, 0);
            server.playSound(null, getX(), getY(), getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8f, 1.0f);
            discard();
        }
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "spin", 0, state -> state.setAndContinue(SPIN)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
