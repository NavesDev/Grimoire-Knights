package com.navesdev.grimoireknights.grimoire.client;

import com.navesdev.grimoireknights.grimoire.CeremonyService;
import net.minecraft.client.Minecraft;
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

/**
 * Client-only grimoire floating beside one player: shows in combat or on cast, turns a page on cast, chooses at the ceremony.
 * Pops in ({@code appear}) and, after closing, shrinks away ({@code vanish}), with a sparkle each time.
 */
public class FloatingGrimoire implements GeoAnimatable {
    static final int VISIBLE_TICKS = 200;
    static final int CLOSE_TICKS = 10;
    static final int VANISH_TICKS = 8;
    /** Ceremony: grows at the shelves ({@code summon}), flies slowly to the player, then {@code choose} and vanishes. */
    static final int CEREMONY_SUMMON_TICKS = 20;
    static final int CEREMONY_FLIGHT_TICKS = 80;
    static final int CEREMONY_CHOOSE_TICKS = CeremonyService.REVEAL_TICKS - CEREMONY_SUMMON_TICKS - CEREMONY_FLIGHT_TICKS;

    private static final RawAnimation FLOAT = RawAnimation.begin().thenLoop("animation.grimoire.float");
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlayAndHold("animation.grimoire.open");
    private static final RawAnimation CLOSE = RawAnimation.begin().thenPlay("animation.grimoire.close");
    // flip ends open, so holding its last frame keeps the book open without replaying open
    private static final RawAnimation FLIP = RawAnimation.begin().thenPlayAndHold("animation.grimoire.flip");
    private static final RawAnimation CHOOSE = RawAnimation.begin().thenPlay("animation.grimoire.choose");
    private static final RawAnimation APPEAR = RawAnimation.begin().thenPlayAndHold("animation.grimoire.appear");
    private static final RawAnimation VANISH = RawAnimation.begin().thenPlayAndHold("animation.grimoire.vanish");
    private static final RawAnimation SUMMON = RawAnimation.begin().thenPlayAndHold("animation.grimoire.summon");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    final int entityId;
    ResourceLocation texture;
    private long tick;
    private long visibleUntil = Long.MIN_VALUE;
    private boolean flipRequested;
    private boolean flipped;
    private boolean sparkle;
    private boolean vanishSparkled;
    private Ceremony ceremony;
    /** Came out of the shelves at the ceremony: grows with {@code summon}, not {@code appear}. */
    private boolean summoned;

    /** Ceremony descent: from the shelves to the player, then {@code choose}. */
    record Ceremony(BlockPos from, long start, ResourceLocation element, int leaves) {}

    FloatingGrimoire(int entityId) {
        this.entityId = entityId;
    }

    void tick(long now) {
        tick = now;
        if (!vanishSparkled && vanishing()) {
            vanishSparkled = true;
            sparkle = true;
        }
    }

    long now() {
        return tick;
    }

    void showFor(int ticks) {
        if (!visible() || vanishing()) sparkle = true;
        visibleUntil = Math.max(visibleUntil, tick + ticks);
        vanishSparkled = false;
    }

    /** True once per appear or vanish, for the sparkle. */
    boolean takeSparkle() {
        boolean was = sparkle;
        sparkle = false;
        return was;
    }

    void flip() {
        showFor(VISIBLE_TICKS);
        flipRequested = true;
    }

    void startCeremony(Ceremony ceremony) {
        this.ceremony = ceremony;
        summoned = true;
        // reaches the player and vanishes, the HUD shows it from then on
        showFor(CeremonyService.REVEAL_TICKS);
    }

    Ceremony ceremony() {
        return ceremony;
    }

    void endCeremony() {
        ceremony = null;
    }

    /** At the shelves or on the way to the player. */
    boolean inFlight() {
        return ceremony != null && tick - ceremony.start() < CEREMONY_SUMMON_TICKS + CEREMONY_FLIGHT_TICKS;
    }

    /** Ceremony grimoire, drawn even in first person until it has vanished. */
    boolean fromCeremony() {
        return summoned && visible();
    }

    boolean open() {
        return tick < visibleUntil && !inFlight();
    }

    /** Closed and shrinking away. */
    boolean vanishing() {
        return visibleUntil != Long.MIN_VALUE && tick >= visibleUntil + CLOSE_TICKS;
    }

    /** Still drawn while it closes and shrinks away. */
    boolean visible() {
        return tick < visibleUntil + CLOSE_TICKS + VANISH_TICKS;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "float", 0, state -> state.setAndContinue(FLOAT)));
        controllers.add(new AnimationController<>(this, "book", 2, this::book));
        controllers.add(new AnimationController<>(this, "ceremony", 0,
                state -> ceremony != null && !inFlight() ? state.setAndContinue(CHOOSE) : PlayState.STOP));
        controllers.add(new AnimationController<>(this, "presence", 0, state -> state.setAndContinue(vanishing() ? VANISH : summoned ? SUMMON : APPEAR)));
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
        // not an entity: GeckoLib takes this as the animation time, so it needs the partial tick to run smooth
        return tick + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
    }
}
