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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

/** Crescent blade: a fast wind sickle that cuts through a few enemies and stops at blocks. */
public class WindBladeEntity extends SpellProjectile {
    private static final RawAnimation SPIN = RawAnimation.begin().thenLoop("animation.wind_blade.spin");
    private final Set<UUID> hit = new HashSet<>();

    public WindBladeEntity(EntityType<? extends WindBladeEntity> type, Level level) {
        super(type, level);
    }

    public WindBladeEntity(Level level, LivingEntity owner) {
        super(AbilityModule.WIND_BLADE.get(), owner, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server && tickCount % 2 == 0) Vfx.play(server, Ids.id("wind_trail"), position(), Map.of());
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && !hit.contains(target.getUUID());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!(level() instanceof ServerLevel server) || !(result.getEntity() instanceof LivingEntity target)) return;
        hit.add(target.getUUID());
        WindService.hurt(server, this, getOwner(), target, AbilityConfig.BLADE_DAMAGE.get());
        if (hit.size() >= AbilityConfig.BLADE_PIERCE.get()) discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide) discard();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "spin", 0, state -> state.setAndContinue(SPIN)));
    }
}
