package com.navesdev.grimoireknights.ability.entity;

import com.navesdev.grimoireknights.ability.AbilityModule;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * White gale bow (Yuno's Shippu no Hakkyu): a huge bow held upright ahead of its caster, a little to the left of the
 * crosshair. It summons and draws six arrows at once, stacked on the string, then fires them together from their
 * spots at the enemy under the crosshair, which they chase; with no enemy, toward where the caster looks. Then it
 * blows away.
 */
public class WindBowEntity extends Entity implements GeoEntity {
    private static final RawAnimation SUMMON = RawAnimation.begin().thenPlay("animation.wind_bow.summon").thenLoop("animation.wind_bow.draw");
    private static final RawAnimation RELEASE = RawAnimation.begin().thenPlay("animation.wind_bow.release");
    private static final EntityDataAccessor<Boolean> RELEASED = SynchedEntityData.defineId(WindBowEntity.class, EntityDataSerializers.BOOLEAN);
    /** Ticks of summon (0.3 s) and draw (0.5 s) before the arrows leave, in step with the cast_c_wind pose. */
    private static final int FIRE_AT = 16;
    /** Ticks of release (0.6 s) before it goes. */
    private static final int RELEASE_TICKS = 12;
    /** Blocks in front of the eyes, to their left and below them; the renderer centers the bow on this spot. */
    private static final double DISTANCE = 1.4;
    private static final double LEFT = 0.5;
    private static final double DROP = 0.2;
    /** Blocks ahead of the eyes the arrows converge on, and how far an enemy can be locked on. */
    private static final double AIM = 24;
    /** Arrows nocked on the string (wind_bow.js ARROWS): height off the grip in blocks, and size (BIG). */
    private static final double[] ARROW_HEIGHTS = {-12.5 / 16, -7.5 / 16, -2.5 / 16, 2.5 / 16, 7.5 / 16, 12.5 / 16};
    private static final double ARROW_AHEAD = 0.5;
    private static final float ARROW_SIZE = 1.5f;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    /** Held directly: the bow is never saved and lives under two seconds. */
    private ServerPlayer owner;
    private int upgrade;

    public WindBowEntity(EntityType<? extends WindBowEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public WindBowEntity(Level level, ServerPlayer owner, int upgrade) {
        this(AbilityModule.WIND_BOW.get(), level);
        this.owner = owner;
        this.upgrade = upgrade;
        follow(owner);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) return;
        if (owner == null || owner.isRemoved() || !owner.isAlive()) {
            discard();
            return;
        }
        if (!entityData.get(RELEASED)) follow(owner);
        if (tickCount == FIRE_AT) fire(server, owner);
        if (tickCount >= FIRE_AT + RELEASE_TICKS) discard();
    }

    private void follow(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 left = Vec3.directionFromRotation(0, player.getYRot() - 90);
        setPos(player.getEyePosition().add(look.scale(DISTANCE)).add(left.scale(LEFT)).subtract(0, DROP, 0));
        setYRot(player.getYRot());
        // projectile convention, as the renderer reads it: positive pitch aims up, the player's looks down
        setXRot(-player.getXRot());
    }

    /** All six at once, each from its spot on the string (model -z forward, +y up). */
    private void fire(ServerLevel server, ServerPlayer player) {
        entityData.set(RELEASED, true);
        LivingEntity target = aimed(player);
        Vec3 point = target != null ? target.getBoundingBox().getCenter() : player.getEyePosition().add(player.getLookAngle().scale(AIM));
        Vec3 forward = player.getLookAngle();
        Vec3 right = Vec3.directionFromRotation(0, player.getYRot() + 90);
        Vec3 up = right.cross(forward);
        for (double height : ARROW_HEIGHTS) {
            Vec3 spot = position().add(up.scale(height)).add(forward.scale(ARROW_AHEAD));
            WindArrowEntity arrow = new WindArrowEntity(server, player, false);
            arrow.withUpgrade(upgrade);
            arrow.withScale(ARROW_SIZE);
            arrow.launch(player, point.subtract(spot).normalize(), AbilityConfig.ARROW_SPEED.get(), 40);
            arrow.setPos(spot);
            arrow.withTarget(target);
            server.addFreshEntity(arrow);
        }
        server.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BREEZE_SHOOT, SoundSource.PLAYERS, 1, 1.2f);
    }

    @Nullable
    private static LivingEntity aimed(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 reach = player.getLookAngle().scale(AIM);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, eye.add(reach), player.getBoundingBox().expandTowards(reach).inflate(1),
                entity -> entity instanceof LivingEntity living && living.isAlive() && !entity.isSpectator() && !entity.isAlliedTo(player), AIM * AIM);
        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RELEASED, false);
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
        controllers.add(new AnimationController<>(this, "main", 0, state -> state.setAndContinue(entityData.get(RELEASED) ? RELEASE : SUMMON)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
