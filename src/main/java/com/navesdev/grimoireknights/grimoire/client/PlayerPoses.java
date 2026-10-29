package com.navesdev.grimoireknights.grimoire.client;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.grimoire.network.PlayerAnimationPayload;
import dev.kosmx.playerAnim.api.IPlayable;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
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
final class PlayerPoses {
    private static final ResourceLocation LAYER = Ids.id("pose");
    private static final int PRIORITY = 1000;

    private PlayerPoses() {}

    static void init() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LAYER, PRIORITY, player -> new ModifierLayer<IAnimation>());
    }

    @SuppressWarnings("unchecked")
    static void onPlay(PlayerAnimationPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !(minecraft.level.getEntity(payload.entityId()) instanceof AbstractClientPlayer player)) return;
        Optional<IPlayable> animation = PlayerAnimationRegistry.getAnimationOptional(payload.animation());
        if (animation.isEmpty()) {
            String known = PlayerAnimationRegistry.getAnimations().keySet().stream()
                    .filter(id -> id.getNamespace().equals(GrimoireKnights.MODID)).map(ResourceLocation::toString).sorted()
                    .collect(Collectors.joining(", "));
            GrimoireKnights.LOGGER.warn("Unknown player animation {}; known: {}", payload.animation(), known);
            if (player == minecraft.player) {
                player.displayClientMessage(Component.translatable(Ids.key("message", "anim.unknown"), payload.animation().toString(), known), false);
            }
            return;
        }
        if (PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER) instanceof ModifierLayer<?> layer) {
            ((ModifierLayer<IAnimation>) layer).setAnimation(animation.get().playAnimation());
        }
    }
}
