package com.navesdev.grimoireknights.ability.entity;

import com.navesdev.grimoireknights.ability.AbilityModule;
import com.navesdev.grimoireknights.ability.Spells;
import com.navesdev.grimoireknights.ability.WindService;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.vfx.Vfx;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * An arrow of the White gale bow, chasing the enemy it was shot at. Upgrade 1: splits in 3 small arrows on hit. Upgrade 2:
 * the small arrows chase the nearest other enemies. Split arrows ({@code split}) don't split again.
 */
public class WindArrowEntity extends SpellProjectile {
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("animation.wind_arrow.fly");
    /** Upgrade 2: blocks around the hit target in which the split arrows look for enemies to chase. */
    private static final double CHASE_RADIUS = 8;
    /** Share of the way it turns toward its target each tick: gentle, so a fanned volley doesn't all land on one enemy. */
    private static final double TURN = 0.1;
    private boolean split;
    private LivingEntity target;

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
        if (target != null) chase();
    }

    /** Chases the target it was shot at (at the same speed) until it dies. */
    public WindArrowEntity withTarget(@Nullable LivingEntity target) {
        this.target = target;
        return this;
    }

    private void chase() {
        if (!target.isAlive() || target.level() != level()) {
            target = null;
            return;
        }
        Vec3 motion = getDeltaMovement();
        Vec3 toward = target.getBoundingBox().getCenter().subtract(position()).normalize();
        Vec3 turned = motion.normalize().lerp(toward, TURN).normalize();
        setDeltaMovement(turned.scale(motion.length()));
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!(level() instanceof ServerLevel server) || !(result.getEntity() instanceof LivingEntity target)) return;
        WindService.hurt(server, this, getOwner(), target, split ? AbilityConfig.ARROW_SPLIT_DAMAGE.get() : AbilityConfig.ARROW_DAMAGE.get());
        if (!split && upgrade() >= 1 && getOwner() instanceof LivingEntity owner) {
            Vec3 forward = getDeltaMovement().normalize();
            // upgrade 2: each piece chases one of the nearest other enemies, nearest first
            List<LivingEntity> others = new ArrayList<>(upgrade() >= 2 ? Spells.around(server, target.position(), CHASE_RADIUS, owner,
                    other -> other != target && !other.isAlliedTo(owner)) : List.of());
            others.sort(Comparator.comparingDouble(other -> other.distanceToSqr(target)));
            int piecesShot = 0;
            for (int angle : new int[] {-20, 0, 20}) {
                WindArrowEntity piece = new WindArrowEntity(server, owner, true);
                piece.withScale(0.5f);
                piece.setPos(target.position().add(0, target.getBbHeight() / 2, 0).add(forward.scale(target.getBbWidth() + 0.3)));
                Vec3 dir = forward.yRot((float) Math.toRadians(angle));
                piece.shoot(dir.x, dir.y, dir.z, AbilityConfig.ARROW_SPEED.get().floatValue(), 0);
                piece.maxTicks = others.isEmpty() ? 8 : 20;
                if (!others.isEmpty()) piece.withTarget(others.get(piecesShot % others.size()));
                piecesShot++;
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
