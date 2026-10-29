package com.navesdev.grimoireknights.grimoire.client;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Client-only grimoire floating beside one player: shows in combat or on cast, turns a page on cast, chooses at the ceremony. */
public class FloatingGrimoire implements GeoAnimatable {
    static final int VISIBLE_TICKS = 200;
    static final int CLOSE_TICKS = 10;
    static final int CEREMONY_FLIGHT_TICKS = 40;
    static final int CEREMONY_CHOOSE_TICKS = 40;

    private static final RawAnimation FLOAT = RawAnimation.begin().thenLoop("animation.grimoire.float");
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlayAndHold("animation.grimoire.open");
    private static final RawAnimation CLOSE = RawAnimation.begin().thenPlay("animation.grimoire.close");
    // flip ends open, so holding its last frame keeps the book open without replaying open
    private static final RawAnimation FLIP = RawAnimation.begin().thenPlayAndHold("animation.grimoire.flip");
    private static final RawAnimation CHOOSE = RawAnimation.begin().thenPlay("animation.grimoire.choose");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    final int entityId;
    ResourceLocation texture;
    private long tick;
    private long visibleUntil = Long.MIN_VALUE;
    private boolean flipRequested;
    private boolean flipped;
    private Ceremony ceremony;

    /** Ceremony descent: from the shelves to the player, then {@code choose}. */
    record Ceremony(BlockPos from, long start, ResourceLocation element, int leaves) {}

    FloatingGrimoire(int entityId) {
        this.entityId = entityId;
    }

    void tick(long now) {
        tick = now;
    }

    long now() {
        return tick;
    }

    void showFor(int ticks) {
        visibleUntil = Math.max(visibleUntil, tick + ticks);
    }

    void flip() {
        showFor(VISIBLE_TICKS);
        flipRequested = true;
    }

    void startCeremony(Ceremony ceremony) {
        this.ceremony = ceremony;
        showFor(CEREMONY_FLIGHT_TICKS + CEREMONY_CHOOSE_TICKS + VISIBLE_TICKS);
    }

    Ceremony ceremony() {
        return ceremony;
    }

    void endCeremony() {
        ceremony = null;
    }

    boolean inFlight() {
        return ceremony != null && tick - ceremony.start() < CEREMONY_FLIGHT_TICKS;
    }

    boolean open() {
        return tick < visibleUntil && !inFlight();
    }

    /** Still drawn while it closes. */
    boolean visible() {
        return tick < visibleUntil + CLOSE_TICKS;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "float", 0, state -> state.setAndContinue(FLOAT)));
        controllers.add(new AnimationController<>(this, "book", 2, this::book));
        controllers.add(new AnimationController<>(this, "ceremony", 0,
                state -> ceremony != null && !inFlight() ? state.setAndContinue(CHOOSE) : PlayState.STOP));
    }

    private PlayState book(AnimationState<FloatingGrimoire> state) {
        if (!open()) {
            flipped = false;
            return state.setAndContinue(CLOSE);
        }
        if (flipRequested) {
            flipRequested = false;
            flipped = true;
            state.getController().forceAnimationReset();
            return state.setAndContinue(FLIP);
        }
        return state.setAndContinue(flipped ? FLIP : OPEN);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object object) {
        return tick;
    }
}
