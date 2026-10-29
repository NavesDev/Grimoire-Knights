package com.navesdev.grimoireknights.grimoire.block;

import com.navesdev.grimoireknights.grimoire.GrimoireModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GrimoireAltarBlockEntity extends BlockEntity implements GeoBlockEntity {
    public static final String ACTIVATE = "activate";
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.grimoire_altar.idle");
    private static final RawAnimation ACTIVATE_ANIM = RawAnimation.begin().thenPlay("animation.grimoire_altar.activate");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GrimoireAltarBlockEntity(BlockPos pos, BlockState state) {
        super(GrimoireModule.ALTAR_BLOCK_ENTITY.get(), pos, state);
    }

    /** Plays {@code activate} for everyone near; call on the server. */
    public void activate() {
        triggerAnim(ACTIVATE, ACTIVATE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 0, state -> state.setAndContinue(IDLE)));
        controllers.add(new AnimationController<>(this, ACTIVATE, 0, state -> PlayState.STOP).triggerableAnim(ACTIVATE, ACTIVATE_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
