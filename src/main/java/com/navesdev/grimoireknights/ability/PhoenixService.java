package com.navesdev.grimoireknights.ability;

import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.vfx.Vfx;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import com.navesdev.grimoireknights.progression.SkillTreeService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** Fire passive: once per fight (a long cooldown), death becomes a revive with half health inside a burning ring. */
public final class PhoenixService {
    private static final ResourceLocation FIRE = Ids.id("fire");
    private static final Map<UUID, Long> READY_AT = new HashMap<>();

    private PhoenixService() {}

    public static boolean ready(ServerPlayer player) {
        return SkillTreeService.hasPassive(player) && GrimoireService.data(player).element().map(FIRE::equals).orElse(false)
                && READY_AT.getOrDefault(player.getUUID(), 0L) <= player.serverLevel().getGameTime();
    }

    /** True when the player revived (the death must be cancelled). */
    public static boolean tryRevive(ServerPlayer player) {
        if (!ready(player)) return false;
        READY_AT.put(player.getUUID(), player.serverLevel().getGameTime() + AbilityConfig.PHOENIX_COOLDOWN_TICKS.get());
        player.setHealth((float) (player.getMaxHealth() * AbilityConfig.PHOENIX_HEALTH.get()));
        player.clearFire();
        BurnService.extinguish(player);
        Spells.around(player.serverLevel(), player.position(), AbilityConfig.PHOENIX_RING_RADIUS.get(), player, target -> true)
                .forEach(target -> BurnService.ignite(target, player));
        Vfx.play(player.serverLevel(), Ids.id("phoenix_revive"), player.position(), Map.of());
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1, 1.2f);
        return true;
    }

    public static void forget(ServerPlayer player) {
        READY_AT.remove(player.getUUID());
    }
}
