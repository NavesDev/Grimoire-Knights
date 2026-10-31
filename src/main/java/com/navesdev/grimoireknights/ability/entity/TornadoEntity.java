package com.navesdev.grimoireknights.ability.entity;

import com.navesdev.grimoireknights.ability.AbilityModule;
import com.navesdev.grimoireknights.ability.HeldService;
import com.navesdev.grimoireknights.ability.WindService;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.vfx.Vfx;
import java.util.Map;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * Tornado fang: a horizontal tornado; the first target it catches is held in it (no moving or attacking).
 * Upgrade 1: mini blades cut the trapped target; upgrade 2: stones hit it on the catch and it is thrown out at the end.
 */
public class TornadoEntity extends SpellProjectile {
    private static final RawAnimation SPIN = RawAnimation.begin().thenLoop("animation.tornado.spin");
    private static final RawAnimation HOLD = RawAnimation.begin().thenLoop("animation.tornado.hold");
    private static final EntityDataAccessor<Boolean> HOLDING = SynchedEntityData.defineId(TornadoEntity.class, EntityDataSerializers.BOOLEAN);
    private LivingEntity target;
    private int heldTicks;

    public TornadoEntity(EntityType<? extends TornadoEntity> type, Level level) {
        super(type, level);
    }

    public TornadoEntity(Level level, LivingEntity owner) {
        super(AbilityModule.TORNADO.get(), owner, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HOLDING, false);
    }

    public boolean holding() {
        return entityData.get(HOLDING);
    }

    @Override
    public void tick() {
        if (target == null) {
            super.tick();
        } else if (level() instanceof ServerLevel server) {
            holdTick(server);
        }
        if (level() instanceof ServerLevel server && tickCount % 2 == 0) Vfx.play(server, Ids.id("tornado_gust"), position(), Map.of());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!(level() instanceof ServerLevel server) || target != null || !(result.getEntity() instanceof LivingEntity caught)) return;
        target = caught;
        setDeltaMovement(Vec3.ZERO);
        setPos(caught.position().add(0, caught.getBbHeight() / 2 - getBbHeight() / 2, 0));
        entityData.set(HOLDING, true);
        HeldService.hold(caught, server.getGameTime() + AbilityConfig.TORNADO_HOLD_TICKS.get());
        WindService.hurt(server, this, getOwner(), caught, AbilityConfig.TORNADO_DAMAGE.get());
        if (upgrade() >= 2) WindService.hurt(server, this, getOwner(), caught, AbilityConfig.TORNADO_STONE_DAMAGE.get());
    }

    private void holdTick(ServerLevel server) {
        heldTicks++;
        if (!target.isAlive() || heldTicks >= AbilityConfig.TORNADO_HOLD_TICKS.get()) {
            release(server);
            return;
        }
        HeldService.pin(target, getX(), getY() + getBbHeight() / 2 - target.getBbHeight() / 2, getZ());
        if (upgrade() >= 1 && heldTicks % 10 == 0) WindService.hurt(server, this, getOwner(), target, AbilityConfig.TORNADO_BLADE_DAMAGE.get());
    }

    private void release(ServerLevel server) {
        HeldService.release(target);
        if (upgrade() >= 2 && target.isAlive()) {
            WindService.hurt(server, this, getOwner(), target, AbilityConfig.TORNADO_STONE_DAMAGE.get());
            Vec3 push = getDeltaMovement().lengthSqr() > 0 ? getDeltaMovement().normalize() : target.position().subtract(position()).normalize();
            target.setDeltaMovement(push.x * 1.2, 0.6, push.z * 1.2);
            target.hurtMarked = true;
        }
        discard();
    }

    @Override
    public void remove(RemovalReason reason) {
        if (target != null) HeldService.release(target);
        super.remove(reason);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide && target == null) discard();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> state.setAndContinue(holding() ? HOLD : SPIN)));
    }
}
