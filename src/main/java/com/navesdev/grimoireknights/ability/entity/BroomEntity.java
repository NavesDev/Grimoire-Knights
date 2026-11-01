package com.navesdev.grimoireknights.ability.entity;

import com.navesdev.grimoireknights.ability.AbilityModule;
import com.navesdev.grimoireknights.ability.AbilityService;
import com.navesdev.grimoireknights.ability.BroomService;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.core.Ids;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.function.ToIntFunction;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * A ridden broom, standing like a hoverboard. The rider steers with the movement keys toward where they look, climbs
 * with jump and sinks with sprint; the
 * client of the rider moves it (vanilla vehicle rule), the server spends mana every second. Out of mana it lets go of
 * its rider, who falls, and falls itself. Without a rider it rests where it is, like a boat: right
 * click to ride it, hit it to pick it back up. It banks into strafes with its rider's lean. The world wears it down
 * like a boat (fire, lava, explosions, cactus, falling blocks...; it hovers, so blocks it would stand on never touch
 * it) and, broken, its rider falls; attacks and spells don't harm it.
 */
public class BroomEntity extends Entity implements GeoEntity {
    private static final RawAnimation HOVER = RawAnimation.begin().thenLoop("animation.broom.hover");
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("animation.broom.fly");
    private static final double MOVING = 0.01;
    /** Degrees it banks into a strafe; the rider is drawn banked with it, standing straight on the handle. */
    private static final float BANK = 16;
    /** Blocks of air it keeps under it while flown, and while parked (it never lies on the ground). */
    private static final double CUSHION = 1.5;
    private static final double PARKED_CUSHION = 0.75;
    /** The rider's climb input, 1 up, -1 down; read on the rider's client, which the client module plugs in. */
    public static ToIntFunction<Player> climbInput = player -> 0;
    public static final TagKey<DamageType> BREAKS = TagKey.create(Registries.DAMAGE_TYPE, Ids.id("breaks_broom"));
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private float lerpYRot;
    private float roll;
    private float rollO;
    private float health = AbilityConfig.BROOM_HEALTH.get().floatValue();

    /** Where the broom moves relative to its rider's facing; picks the rider's pose and the broom's bank. */
    public enum Lean { HOVER, FORWARD, BACKWARD, LEFT, RIGHT }

    public BroomEntity(EntityType<? extends BroomEntity> type, Level level) {
        super(type, level);
    }

    public BroomEntity(Level level, Vec3 position, float yaw) {
        this(AbilityModule.BROOM.get(), level);
        setPos(position);
        setYRot(yaw);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity rider = getControllingPassenger();
        if (!level().isClientSide) {
            if (rider instanceof ServerPlayer player && tickCount % 20 == 0 && !BroomService.payFlight(player)) {
                BroomService.drop(player);
                return;
            }
        }
        if (level().isClientSide) bank(rider);
        tickLerp();
        if (rider != null) rider.fallDistance = 0;
        fallDistance = 0;
        // whoever controls it moves it (the rider's client, or the server once riderless); the others ease toward its updates
        if (!isControlledByLocalInstance()) return;
        if (rider instanceof Player player) steer(player);
        else if (rider == null) rest();
    }

    /** As the boat: the controlling side keeps the packet position base current (or updates after a flight decode from
     *  where it was mounted, and the broom jumps); the other side steps toward the last update. */
    private void tickLerp() {
        if (isControlledByLocalInstance()) {
            lerpSteps = 0;
            syncPacketPositionCodec(getX(), getY(), getZ());
        }
        if (lerpSteps > 0) {
            lerpPositionAndRotationStep(lerpSteps, lerpX, lerpY, lerpZ, lerpYRot, getXRot());
            lerpSteps--;
        }
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        lerpX = x;
        lerpY = y;
        lerpZ = z;
        lerpYRot = yRot;
        lerpSteps = 3;
    }

    @Override
    public double lerpTargetX() {
        return lerpSteps > 0 ? lerpX : getX();
    }

    @Override
    public double lerpTargetY() {
        return lerpSteps > 0 ? lerpY : getY();
    }

    @Override
    public double lerpTargetZ() {
        return lerpSteps > 0 ? lerpZ : getZ();
    }

    @Override
    public float lerpTargetYRot() {
        return lerpSteps > 0 ? lerpYRot : getYRot();
    }

    /** Forward and back along the look (up and down with it), strafing sideways, climbing or sinking straight, over a
     *  cushion of air (CUSHION). */
    private void steer(Player rider) {
        double speed = AbilityConfig.BROOM_SPEED.get();
        Vec3 look = rider.getLookAngle();
        Vec3 side = new Vec3(-look.z, 0, look.x).normalize();
        Vec3 wanted = look.scale(rider.zza > 0 ? speed : rider.zza < 0 ? -speed * 0.5 : 0).add(side.scale(-rider.xxa * speed * 0.6))
                .add(0, climbInput.applyAsInt(rider) * AbilityConfig.BROOM_CLIMB_SPEED.get(), 0);
        Vec3 motion = getDeltaMovement().scale(0.6).add(wanted.scale(0.4));
        double clearance = clearance(CUSHION);
        // well under the cushion it lifts; near its top it only stops sinking, so it holds level instead of bobbing
        if (clearance < CUSHION - 0.2) motion = new Vec3(motion.x, Math.max(motion.y, (CUSHION - 0.1 - clearance) * 0.5), motion.z);
        else if (clearance < CUSHION) motion = new Vec3(motion.x, Math.max(motion.y, 0), motion.z);
        setDeltaMovement(motion);
        setYRot(rider.getYRot());
        move(MoverType.SELF, getDeltaMovement());
    }

    /** Air under it, in steps of a tenth of a block up to {@code limit}. */
    private double clearance(double limit) {
        for (double drop = 0.1; drop <= limit + 1e-6; drop += 0.1) {
            if (!level().noCollision(this, getBoundingBox().move(0, -drop, 0))) return drop - 0.1;
        }
        return limit;
    }

    /** Riderless: falls like anything else, then settles floating a little above the ground and slides to a stop. Inside
     *  a band around the float height it holds still, so it doesn't bob between falling and lifting. */
    private void rest() {
        Vec3 motion = getDeltaMovement().multiply(0.8, 0.98, 0.8);
        double clearance = clearance(PARKED_CUSHION + 0.5);
        double vertical;
        if (clearance > PARKED_CUSHION + 0.4) vertical = motion.y - 0.04;
        else if (clearance < PARKED_CUSHION - 0.1) vertical = 0.05;
        else if (clearance > PARKED_CUSHION + 0.1) vertical = -0.05;
        else vertical = 0;
        boolean floating = clearance <= PARKED_CUSHION + 0.4;
        setDeltaMovement(floating ? new Vec3(motion.x * 0.5, vertical, motion.z * 0.5) : new Vec3(motion.x, vertical, motion.z));
        Vec3 now = getDeltaMovement();
        if (now.lengthSqr() > 1e-6) move(MoverType.SELF, now);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive() || isVehicle()) return InteractionResult.PASS;
        if (player instanceof ServerPlayer server) BroomService.mount(server, this);
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    /** The world's damage (tag {@code breaks_broom}) wears it down, ridden or not; a player's hit on a parked broom picks
     *  it back up (nothing drops in creative); nothing else harms it. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || isRemoved()) return false;
        if (source.is(BREAKS)) {
            wear(source, amount);
            return true;
        }
        if (isVehicle() || !(source.getEntity() instanceof Player player)) return false;
        if (!player.getAbilities().instabuild) spawnAtLocation(AbilityModule.BROOM_ITEM.get());
        discard();
        return true;
    }

    /** Broken: the rider falls, and the broom is gone, in smoke when it burnt. */
    private void wear(DamageSource source, float amount) {
        health -= amount;
        if (health > 0) return;
        boolean burnt = source.is(DamageTypeTags.IS_FIRE);
        if (getControllingPassenger() instanceof ServerPlayer rider) {
            rider.displayClientMessage(Component.translatable(Ids.key("message", burnt ? "broom.burned" : "broom.broken")).withStyle(ChatFormatting.RED), true);
        }
        ejectPassengers();
        level().playSound(null, getX(), getY(), getZ(), burnt ? SoundEvents.GENERIC_BURN : SoundEvents.ITEM_BREAK, SoundSource.NEUTRAL, 1, 1);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(burnt ? ParticleTypes.LARGE_SMOKE : ParticleTypes.POOF, getX(), getY(), getZ(), 12, 0.3, 0.1, 0.3, 0.02);
        }
        discard();
    }

    public float health() {
        return health;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(AbilityModule.BROOM_ITEM.get());
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

    /** Bank around its flight axis in world space, the top toward the strafe; the renderer turns broom and rider by it. */
    public Quaternionf bank(float partialTick) {
        Vec3 facing = Vec3.directionFromRotation(0, Mth.rotLerp(partialTick, yRotO, getYRot()));
        return new Quaternionf().rotationAxis(roll(partialTick) * Mth.DEG_TO_RAD, (float) facing.x, 0, (float) facing.z);
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
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("health")) health = tag.getFloat("health");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("health", health);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4,
                state -> state.setAndContinue(getDeltaMovement().horizontalDistanceSqr() > MOVING ? FLY : HOVER)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /** Mana a second of flight costs this rider, after Mana Control. */
    public static double manaPerSecond(Player rider) {
        return AbilityService.cost(rider, AbilityConfig.BROOM_MANA_PER_SECOND.get());
    }
}
