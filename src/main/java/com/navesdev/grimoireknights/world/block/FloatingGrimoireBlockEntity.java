package com.navesdev.grimoireknights.world.block;

import com.navesdev.grimoireknights.world.WorldModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FloatingGrimoireBlockEntity extends BlockEntity implements GeoBlockEntity {
    private static final RawAnimation FLOAT = RawAnimation.begin().thenLoop("animation.grimoire.float");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public FloatingGrimoireBlockEntity(BlockPos pos, BlockState state) {
        super(WorldModule.FLOATING_GRIMOIRE_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "float", 0, state -> state.setAndContinue(FLOAT)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
