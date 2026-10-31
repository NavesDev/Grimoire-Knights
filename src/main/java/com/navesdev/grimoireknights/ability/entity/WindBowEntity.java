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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** White gale bow: floats in front of its caster and shoots its arrows in sequence toward where they look, then fades. */
public class WindBowEntity extends Entity implements GeoEntity {
    private static final RawAnimation SUMMON = RawAnimation.begin().thenPlay("animation.wind_bow.summon").thenLoop("animation.wind_bow.draw");
    private static final int WARMUP = 6;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID owner;
    private int upgrade;

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
        setPos(player.getEyePosition().add(look.scale(1.2)).subtract(0, 0.4, 0));
        setYRot(player.getYRot());
        setXRot(player.getXRot());
    }

    private void shoot(ServerLevel server, ServerPlayer player) {
        WindArrowEntity arrow = new WindArrowEntity(server, player, false);
        arrow.withUpgrade(upgrade);
        arrow.launch(player, player.getLookAngle(), AbilityConfig.ARROW_SPEED.get(), 40);
        server.addFreshEntity(arrow);
        server.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BREEZE_SHOOT, SoundSource.PLAYERS, 0.6f, 1.5f);
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
