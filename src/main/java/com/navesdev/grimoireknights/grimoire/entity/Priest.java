package com.navesdev.grimoireknights.grimoire.entity;

import com.navesdev.grimoireknights.grimoire.CeremonyService;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Village priest: explains the grimoire calling and, next to a Grimoire Altar, holds the ceremony.
 * Stays where it was placed (the tower or the church), walking back if pushed away; it cannot be hurt (only /kill),
 * so the ceremony is never lost.
 */
public class Priest extends PathfinderMob implements GeoEntity {
    private static final String HOME = "GkHome";
    private static final int HOME_RADIUS = 2;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.priest.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.priest.walk");
    private static final RawAnimation BLESS = RawAnimation.begin().thenPlay("animation.priest.bless");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    /** Where the priest was placed; the ceremony looks for the altar around it. */
    private BlockPos home;

    public Priest(EntityType<? extends Priest> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MoveTowardsRestrictionGoal(this, 0.6));
        goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8f));
        goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    public BlockPos home() {
        if (home == null) home = blockPosition();
        return home;
    }

    @Override
    public void tick() {
        if (!level().isClientSide && !hasRestriction()) restrictTo(home(), HOME_RADIUS);
        super.tick();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong(HOME, home().asLong());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(HOME)) home = BlockPos.of(tag.getLong(HOME));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (player instanceof ServerPlayer serverPlayer) {
            triggerAnim("action", "bless");
            CeremonyService.talk(serverPlayer, this);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "move", 4, state -> state.setAndContinue(state.isMoving() ? WALK : IDLE)));
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("bless", BLESS));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
