package com.navesdev.grimoireknights.ability.entity;

import com.navesdev.grimoireknights.ability.AbilityModule;
import com.navesdev.grimoireknights.ability.BurnService;
import com.navesdev.grimoireknights.ability.HeldService;
import com.navesdev.grimoireknights.ability.Spells;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.vfx.Vfx;
import com.navesdev.grimoireknights.grimoire.GrimoireActivity;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * Flame arm: a giant open hand of fire. It grabs the first target, lifts it and slams it into the ground (the target
 * can't act meanwhile); the slam explodes with damage, knockback and burn. A block or the end of its range explodes it there.
 */
public class FlameFistEntity extends SpellProjectile {
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("animation.flame_fist.fly");
    private static final RawAnimation GRAB = RawAnimation.begin().thenPlay("animation.flame_fist.grab");
    private static final RawAnimation SLAM = RawAnimation.begin().thenPlay("animation.flame_fist.slam");
    private static final RawAnimation IMPACT = RawAnimation.begin().thenPlay("animation.flame_fist.impact");
    private LivingEntity target;
    private int heldTicks;
    /** The slam pose was sent; it ends on its own. */
    private boolean slammed;
    private double baseY;
    private double groundY;

    public FlameFistEntity(EntityType<? extends FlameFistEntity> type, Level level) {
        super(type, level);
    }

    public FlameFistEntity(Level level, LivingEntity owner) {
        super(AbilityModule.FLAME_FIST.get(), owner, level);
    }

    @Override
    public void tick() {
        if (target == null) super.tick();
        else if (level() instanceof ServerLevel server) holdTick(server);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!(level() instanceof ServerLevel server) || target != null || !(result.getEntity() instanceof LivingEntity caught)) return;
        target = caught;
        baseY = caught.getY();
        BlockPos below = caught.blockPosition();
        groundY = server.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, below.getX(), below.getZ());
        if (groundY > baseY + 1) groundY = baseY;
        setDeltaMovement(Vec3.ZERO);
        HeldService.hold(caught, server.getGameTime() + AbilityConfig.FLAME_ARM_HOLD_TICKS.get() + 2);
        triggerAnim("main", "grab");
    }

    private void holdTick(ServerLevel server) {
        heldTicks++;
        int hold = AbilityConfig.FLAME_ARM_HOLD_TICKS.get();
        int lift = hold / 2;
        if (heldTicks == lift) {
            triggerAnim("main", "slam");
            slammed = true;
            if (getOwner() instanceof ServerPlayer owner) GrimoireActivity.pose(owner, Ids.id("cast_x_fire_slam"));
        }
        double y = heldTicks <= lift
                ? baseY + AbilityConfig.FLAME_ARM_LIFT.get() * heldTicks / lift
                : baseY + AbilityConfig.FLAME_ARM_LIFT.get() - (baseY + AbilityConfig.FLAME_ARM_LIFT.get() - groundY) * (heldTicks - lift) / (double) (hold - lift);
        if (target.isAlive()) HeldService.pin(target, target.getX(), y, target.getZ());
        setPos(target.getX(), y + target.getBbHeight() / 2 - getBbHeight() / 2, target.getZ());
        if (heldTicks >= hold || !target.isAlive()) {
            HeldService.release(target);
            explode(server, new Vec3(target.getX(), groundY, target.getZ()));
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level() instanceof ServerLevel server && target == null) explode(server, result.getLocation());
    }

    @Override
    protected void outOfRange() {
        if (level() instanceof ServerLevel server) explode(server, position());
    }

    /** Radius grows with upgrade 1. Everyone in it but the caster takes the damage, knockback and burn. */
    private void explode(ServerLevel server, Vec3 center) {
        double radius = upgrade() >= 1 ? AbilityConfig.FLAME_ARM_BIG_RADIUS.get() : AbilityConfig.FLAME_ARM_RADIUS.get();
        DamageSource source = Spells.source(server, Spells.FIRE, this, getOwner());
        for (LivingEntity hit : Spells.around(server, center, radius, getOwner(), living -> true)) {
            if (hit.hurt(source, AbilityConfig.FLAME_ARM_DAMAGE.get().floatValue())) {
                Vec3 away = hit.position().subtract(center).multiply(1, 0, 1);
                if (away.lengthSqr() < 1e-4) away = new Vec3(0, 0, 0);
                else away = away.normalize();
                double force = AbilityConfig.FLAME_ARM_KNOCKBACK.get();
                hit.setDeltaMovement(hit.getDeltaMovement().add(away.x * force, 0.4 * force, away.z * force));
                hit.hurtMarked = true;
                BurnService.ignite(hit, getOwner());
            }
        }
        triggerAnim("main", "impact");
        Vfx.play(server, Ids.id("flame_fist_explosion"), center, Map.of("radius", (float) radius));
        server.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.7f, 1.1f);
        discard();
    }

    @Override
    public void remove(RemovalReason reason) {
        if (target != null) HeldService.release(target);
        // the caster holds the arm out (cast_x_fire holds its last frame) until the slam; gone any other way, it drops it
        if (!level().isClientSide && !slammed && getOwner() instanceof ServerPlayer owner) GrimoireActivity.pose(owner, Ids.id("cast_x_fire_release"));
        super.remove(reason);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 2, state -> state.setAndContinue(FLY))
                .triggerableAnim("grab", GRAB).triggerableAnim("slam", SLAM).triggerableAnim("impact", IMPACT));
    }
}
