package com.navesdev.grimoireknights.ability.entity;

import com.navesdev.grimoireknights.ability.AbilityModule;
import com.navesdev.grimoireknights.ability.BurnService;
import com.navesdev.grimoireknights.ability.Delayed;
import com.navesdev.grimoireknights.ability.Spells;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.vfx.Vfx;
import java.util.Map;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

/** The fireball: flies straight (no gravity) and explodes on what it hits, hurting and burning around; fades out at the end of its range. */
public class FireballProjectile extends SpellProjectile {
    private static final ResourceLocation EXPLOSION_VFX = Ids.id("fireball_explosion");
    private static final ResourceLocation GROUND_FIRE_VFX = Ids.id("fireball_ground_fire");
    private static final int FADE_TICKS = 8;
    private static final RawAnimation SPIN = RawAnimation.begin().thenLoop("animation.fireball.spin");

    public FireballProjectile(EntityType<? extends FireballProjectile> type, Level level) {
        super(type, level);
        maxTicks = AbilityConfig.FIREBALL_LIFETIME_TICKS.get();
    }

    public FireballProjectile(Level level, LivingEntity owner) {
        super(AbilityModule.FIREBALL.get(), owner, level);
        maxTicks = AbilityConfig.FIREBALL_LIFETIME_TICKS.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (isRemoved()) return;
        if (level().isClientSide) {
            Vec3 motion = getDeltaMovement();
            for (int i = 0; i < 2; i++) {
                if (random.nextFloat() > fade(0)) continue;
                level().addParticle(i == 0 ? ParticleTypes.FLAME : ParticleTypes.SMOKE,
                        getX() - motion.x * 0.5 + (random.nextDouble() - 0.5) * 0.3,
                        getY() + 0.25 - motion.y * 0.5 + (random.nextDouble() - 0.5) * 0.3,
                        getZ() - motion.z * 0.5 + (random.nextDouble() - 0.5) * 0.3, 0, 0.01, 0);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) explode(result instanceof EntityHitResult hit ? hit.getEntity() : null);
    }

    /**
     * Blows up where it is, breaking nothing: {@code direct} (what it hit, if anything) takes the full damage, the
     * others in the radius the area damage; all of them burn. The caster is never hurt. Upgrade 1 grows the radius
     * with the size; upgrade 2 leaves fire on the ground that burns whoever stands in it.
     */
    public void explode(Entity direct) {
        if (!(level() instanceof ServerLevel server)) return;
        double radius = AbilityConfig.FIREBALL_RADIUS.get() * scale();
        Vec3 center = position().add(0, getBbHeight() / 2, 0);
        DamageSource source = new DamageSource(server.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(AbilityModule.FIREBALL_DAMAGE), this, getOwner());
        if (direct != null && direct != getOwner()) hurt(direct, source, AbilityConfig.FIREBALL_DAMAGE.get());
        for (LivingEntity target : Spells.around(server, center, radius, getOwner(), target -> target != direct)) {
            hurt(target, source, AbilityConfig.FIREBALL_AREA_DAMAGE.get());
        }
        Vfx.play(server, EXPLOSION_VFX, center, Map.of("radius", (float) radius));
        if (upgrade() >= 2) groundFire(server, center, radius, getOwner());
        server.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.6f, 1.4f);
        server.playSound(null, center.x, center.y, center.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8f, 0.8f);
        discard();
    }

    private void hurt(Entity target, DamageSource source, double damage) {
        if (target.hurt(source, (float) damage) && target instanceof LivingEntity living) BurnService.ignite(living, getOwner());
    }

    /** Burns whoever stands in the radius every half second for the ground fire's duration. */
    private static void groundFire(ServerLevel server, Vec3 center, double radius, Entity owner) {
        Vfx.play(server, GROUND_FIRE_VFX, center, Map.of("radius", (float) radius));
        for (int t = 0; t < AbilityConfig.GROUND_FIRE_TICKS.get(); t += 10) {
            Delayed.run(server, t, () -> Spells.around(server, center, radius, owner, living -> living.onGround())
                    .forEach(living -> BurnService.ignite(living, owner)));
        }
    }

    /** 1 while flying, falling to 0 over the last {@link #FADE_TICKS} of its range: the renderer fades and shrinks it. */
    public float fade(float partialTick) {
        float left = maxTicks - tickCount - partialTick;
        return Math.clamp(left / FADE_TICKS, 0, 1);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "spin", 0, state -> state.setAndContinue(SPIN)));
    }
}
