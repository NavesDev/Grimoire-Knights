package com.navesdev.grimoireknights.grimoire.client;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.grimoire.network.PlayerAnimationPayload;
import dev.kosmx.playerAnim.api.IPlayable;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** playerAnimator poses on players: one layer per player, played by id from {@code assets/<ns>/player_animation/}. */
public final class PlayerPoses {
    private static final ResourceLocation LAYER = Ids.id("pose");
    private static final int PRIORITY = 1000;

    private PlayerPoses() {}

    static void init() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LAYER, PRIORITY, player -> new ModifierLayer<IAnimation>());
    }

    static void onPlay(PlayerAnimationPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.level.getEntity(payload.entityId()) instanceof AbstractClientPlayer player) play(player, payload.animation());
    }

    /** Stops whatever pose the player is in (back to the vanilla pose). */
    @SuppressWarnings("unchecked")
    public static void stop(AbstractClientPlayer player) {
        if (PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER) instanceof ModifierLayer<?> layer) {
            ((ModifierLayer<IAnimation>) layer).setAnimation(null);
        }
    }

    public static void play(AbstractClientPlayer player, ResourceLocation id) {
        play(player, id, 0);
    }

    /** Plays the pose blending into it from the current one over {@code fadeTicks} (0 cuts straight to it). */
    @SuppressWarnings("unchecked")
    public static void play(AbstractClientPlayer player, ResourceLocation id, int fadeTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        Optional<IPlayable> animation = resolve(id);
        if (animation.isEmpty()) {
            String known = PlayerAnimationRegistry.getAnimations().keySet().stream()
                    .filter(key -> key.getNamespace().equals(GrimoireKnights.MODID)).map(ResourceLocation::toString).sorted()
                    .collect(Collectors.joining(", "));
            GrimoireKnights.LOGGER.warn("Unknown player animation {}; known: {}", id, known);
            if (player == minecraft.player) {
                player.displayClientMessage(Component.translatable(Ids.key("message", "anim.unknown"), id.toString(), known), false);
            }
            return;
        }
        if (PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER) instanceof ModifierLayer<?> layer) {
            ModifierLayer<IAnimation> modifier = (ModifierLayer<IAnimation>) layer;
            if (fadeTicks > 0) modifier.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fadeTicks, Ease.INOUTSINE), animation.get().playAnimation());
            else modifier.setAnimation(animation.get().playAnimation());
        }
    }

    /** Exact id, or a short id like {@code grimoireknights:dash} matching {@code animation.player_cast.dash} in the same namespace. */
    private static Optional<IPlayable> resolve(ResourceLocation id) {
        Optional<IPlayable> exact = PlayerAnimationRegistry.getAnimationOptional(id);
        if (exact.isPresent()) return exact;
        return PlayerAnimationRegistry.getAnimations().entrySet().stream()
                .filter(entry -> entry.getKey().getNamespace().equals(id.getNamespace()) && entry.getKey().getPath().endsWith("." + id.getPath()))
                .map(java.util.Map.Entry::getValue).findFirst();
    }
}
