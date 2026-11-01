package com.navesdev.grimoireknights.ability.entity;

import com.navesdev.grimoireknights.ability.AbilityModule;
import com.navesdev.grimoireknights.ability.WindService;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.vfx.Vfx;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
 * Tornado fang (Yuno's Boran no Kiba): a horizontal tornado that drills through every enemy in its way, hitting each
 * once and pushing it along its flight, until its range ends or it meets a block.
 * Upgrade 1: mini blades cut each enemy it passes; upgrade 2: stones hit them too and the push is stronger.
 */
public class TornadoEntity extends SpellProjectile {
    private static final RawAnimation SPIN = RawAnimation.begin().thenLoop("animation.tornado.spin");
    private static final double STONE_PUSH = 1.5;
    private static final double LIFT = 0.3;
    private final Set<UUID> hit = new HashSet<>();

    public TornadoEntity(EntityType<? extends TornadoEntity> type, Level level) {
        super(type, level);
    }

    public TornadoEntity(Level level, LivingEntity owner) {
        super(AbilityModule.TORNADO.get(), owner, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || isRemoved()) return;
        // the whole funnel hits, not only the ray the projectile flies along
        for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox(), this::canHitEntity)) {
            if (hit.add(target.getUUID())) drill(server, target);
        }
        if (tickCount % 2 == 0) Vfx.play(server, Ids.id("tornado_gust"), position(), Map.of());
    }

    private void drill(ServerLevel server, LivingEntity target) {
        double damage = AbilityConfig.TORNADO_DAMAGE.get();
        if (upgrade() >= 1) damage += AbilityConfig.TORNADO_BLADE_DAMAGE.get();
        if (upgrade() >= 2) damage += AbilityConfig.TORNADO_STONE_DAMAGE.get();
        WindService.hurt(server, this, getOwner(), target, damage);
        Vec3 flight = getDeltaMovement().lengthSqr() > 1e-6 ? getDeltaMovement().normalize() : Vec3.ZERO;
        Vec3 push = flight.scale(AbilityConfig.TORNADO_PUSH.get() * (upgrade() >= 2 ? STONE_PUSH : 1));
        target.setDeltaMovement(push.x, Math.max(push.y, LIFT), push.z);
        target.hurtMarked = true;
    }

    /** Entities are drilled by {@link #tick}: the hit doesn't stop the tornado. */
    @Override
    protected void onHitEntity(EntityHitResult result) {}

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide) discard();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> state.setAndContinue(SPIN)));
    }
}
