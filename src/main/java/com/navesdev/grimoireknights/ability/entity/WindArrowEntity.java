package com.navesdev.grimoireknights.ability.entity;

import com.navesdev.grimoireknights.ability.AbilityModule;
import com.navesdev.grimoireknights.ability.WindService;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.vfx.Vfx;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * An arrow of the White gale bow. Upgrade 1: splits in 3 small arrows on hit. Upgrade 2: destroys enemy projectiles it passes.
 * Split arrows ({@code split}) do neither.
 */
public class WindArrowEntity extends SpellProjectile {
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("animation.wind_arrow.fly");
    private static final double INTERCEPT = 1.5;
    private boolean split;

    public WindArrowEntity(EntityType<? extends WindArrowEntity> type, Level level) {
        super(type, level);
    }

    public WindArrowEntity(Level level, LivingEntity owner, boolean split) {
        super(AbilityModule.WIND_ARROW.get(), owner, level);
        this.split = split;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || isRemoved()) return;
        if (tickCount % 2 == 0) Vfx.play(server, Ids.id("wind_trail"), position(), Map.of());
        if (!split && upgrade() >= 2) {
            for (Projectile enemy : server.getEntitiesOfClass(Projectile.class, getBoundingBox().inflate(INTERCEPT), this::interceptable)) {
                Vfx.play(server, Ids.id("wind_hit"), enemy.position(), Map.of("bonus", 0f));
                enemy.discard();
            }
        }
    }

    /** Hostile arrows, fireballs and spells only: never tridents (the item would be lost), pearls, hooks, or allies' shots. */
    private boolean interceptable(Projectile other) {
        if (!other.isAlive() || other instanceof WindArrowEntity || other instanceof ThrownTrident) return false;
        if (!(other instanceof AbstractArrow || other instanceof AbstractHurtingProjectile || other instanceof SpellProjectile)) return false;
        Entity shooter = other.getOwner();
        return shooter != getOwner() && !(shooter != null && getOwner() != null && shooter.isAlliedTo(getOwner()));
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!(level() instanceof ServerLevel server) || !(result.getEntity() instanceof LivingEntity target)) return;
        WindService.hurt(server, this, getOwner(), target, split ? AbilityConfig.ARROW_SPLIT_DAMAGE.get() : AbilityConfig.ARROW_DAMAGE.get());
        if (!split && upgrade() >= 1 && getOwner() instanceof LivingEntity owner) {
            Vec3 forward = getDeltaMovement().normalize();
            for (int angle : new int[] {-20, 0, 20}) {
                WindArrowEntity piece = new WindArrowEntity(server, owner, true);
                piece.withScale(0.5f);
                piece.setPos(target.position().add(0, target.getBbHeight() / 2, 0).add(forward.scale(target.getBbWidth() + 0.3)));
                Vec3 dir = forward.yRot((float) Math.toRadians(angle));
                piece.shoot(dir.x, dir.y, dir.z, AbilityConfig.ARROW_SPEED.get().floatValue(), 0);
                piece.maxTicks = 8;
                server.addFreshEntity(piece);
            }
        }
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide) discard();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "fly", 0, state -> state.setAndContinue(FLY)));
    }
}
