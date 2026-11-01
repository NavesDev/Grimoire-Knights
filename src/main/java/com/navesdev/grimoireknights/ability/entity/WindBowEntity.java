package com.navesdev.grimoireknights.ability.entity;

import com.navesdev.grimoireknights.ability.AbilityModule;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
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
 * White gale bow (Yuno's Shippu no Hakkyu): floats in front of its caster, a little away, and shoots its arrows in
 * sequence from itself at the enemy under the crosshair, which they chase; with no enemy, toward where they look. Then fades.
 */
public class WindBowEntity extends Entity implements GeoEntity {
    private static final RawAnimation SUMMON = RawAnimation.begin().thenPlay("animation.wind_bow.summon").thenLoop("animation.wind_bow.draw");
    private static final int WARMUP = 6;
    /** Blocks in front of the eyes and below them; the renderer centers the model on this spot. */
    private static final double DISTANCE = 2.2;
    private static final double DROP = 0.3;
    /** Blocks ahead of the eyes the arrows converge on, and how far an enemy can be locked on. */
    private static final double AIM = 24;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID owner;
    private int upgrade;
    private LivingEntity target;

    public WindBowEntity(EntityType<? extends WindBowEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public WindBowEntity(Level level, ServerPlayer owner, int upgrade) {
        this(AbilityModule.WIND_BOW.get(), level);
        this.owner = owner.getUUID();
        this.upgrade = upgrade;
        follow(owner);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) return;
        if (!(owner != null && server.getEntity(owner) instanceof ServerPlayer player) || !player.isAlive()) {
            discard();
            return;
        }
        follow(player);
        int arrows = AbilityConfig.BOW_ARROWS.get();
        int interval = AbilityConfig.BOW_INTERVAL_TICKS.get();
        int shot = tickCount - WARMUP;
        if (shot >= 0 && shot % interval == 0 && shot / interval < arrows) shoot(server, player);
        if (shot >= arrows * interval + 4) discard();
    }

    private void follow(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        setPos(player.getEyePosition().add(look.scale(DISTANCE)).subtract(0, DROP, 0));
        setYRot(player.getYRot());
        setXRot(player.getXRot());
    }

    private void shoot(ServerLevel server, ServerPlayer player) {
        WindArrowEntity arrow = new WindArrowEntity(server, player, false);
        arrow.withUpgrade(upgrade);
        // locks on the enemy under the crosshair (keeping the last one when there is none); with no lock it flies from
        // the bow toward the point the caster looks at, so the arrow lands under the crosshair
        LivingEntity aimed = aimed(player);
        if (aimed != null) target = aimed;
        if (target != null && !target.isAlive()) target = null;
        Vec3 point = target != null ? target.getBoundingBox().getCenter() : player.getEyePosition().add(player.getLookAngle().scale(AIM));
        arrow.launch(player, point.subtract(position()).normalize(), AbilityConfig.ARROW_SPEED.get(), 40);
        arrow.setPos(position());
        arrow.withTarget(target);
        server.addFreshEntity(arrow);
        server.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BREEZE_SHOOT, SoundSource.PLAYERS, 0.6f, 1.5f);
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

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
        controllers.add(new AnimationController<>(this, "main", 0, state -> state.setAndContinue(SUMMON)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
