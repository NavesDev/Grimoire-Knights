package com.navesdev.grimoireknights.ability;

import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.ability.network.TailwindPayload;
import com.navesdev.grimoireknights.ability.rules.WindRules;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.vfx.Vfx;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import com.navesdev.grimoireknights.progression.SkillTreeService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.network.PacketDistributor;

/** Wind passive: each wind hit adds a stack (speed up, wind cooldowns down); all drop a while after the last hit. */
public final class TailwindService {
    private static final ResourceLocation SPEED = Ids.id("tailwind");
    public static final ResourceLocation WIND = Ids.id("wind");

    private record State(int stacks, long lastHit) {}

    private static final Map<UUID, State> STATES = new HashMap<>();

    private TailwindService() {}

    public static int stacks(ServerPlayer player) {
        State state = STATES.get(player.getUUID());
        return state == null ? 0 : state.stacks();
    }

    public static void onHit(ServerPlayer player) {
        if (!SkillTreeService.hasPassive(player) || !GrimoireService.data(player).element().map(WIND::equals).orElse(false)) return;
        int next = WindRules.addStack(stacks(player), AbilityConfig.TAILWIND_MAX_STACKS.get());
        STATES.put(player.getUUID(), new State(next, player.serverLevel().getGameTime()));
        apply(player, next);
    }

    /** Wind spell cooldowns shortened by the stacks. */
    public static int cooldown(ServerPlayer player, int ticks) {
        return WindRules.cooldown(ticks, stacks(player), AbilityConfig.TAILWIND_COOLDOWN_CUT.get());
    }

    public static void tick(ServerPlayer player) {
        State state = STATES.get(player.getUUID());
        if (state == null) return;
        long now = player.serverLevel().getGameTime();
        if (now - state.lastHit() > AbilityConfig.TAILWIND_DURATION_TICKS.get() || !player.isAlive()) {
            STATES.remove(player.getUUID());
            apply(player, 0);
        } else if ((now - state.lastHit()) % 20 == 0) {
            Vfx.play(player.serverLevel(), Ids.id("tailwind_aura"), player.position(), Map.of("stacks", (float) state.stacks()));
        }
    }

    public static void forget(ServerPlayer player) {
        STATES.remove(player.getUUID());
        apply(player, 0);
    }

    private static void apply(ServerPlayer player, int stacks) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SPEED);
            if (stacks > 0) speed.addTransientModifier(new AttributeModifier(SPEED,
                    stacks * AbilityConfig.TAILWIND_SPEED_PER_STACK.get(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        PacketDistributor.sendToPlayer(player, new TailwindPayload(stacks));
    }
}
