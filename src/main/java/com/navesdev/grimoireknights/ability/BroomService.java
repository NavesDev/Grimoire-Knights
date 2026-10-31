package com.navesdev.grimoireknights.ability;

import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.ability.entity.BroomEntity;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.progression.SkillEffects;
import com.navesdev.grimoireknights.progression.SkillTreeService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Broom flight: any elemental grimoire with Mana Control III; it spends mana every second. */
public final class BroomService {
    public enum Result { OK, CANNOT_ACT, NO_GRIMOIRE, NO_MANA_CONTROL, NO_MANA }

    private BroomService() {}

    public static Result check(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator() || player.isPassenger() || HeldService.held(player)) return Result.CANNOT_ACT;
        if (com.navesdev.grimoireknights.grimoire.GrimoireService.element(player).isEmpty()) return Result.NO_GRIMOIRE;
        if (SkillTreeService.effectRank(player, SkillEffects.MANA_CONTROL) < AbilityConfig.BROOM_MANA_CONTROL.get()) return Result.NO_MANA_CONTROL;
        AbilityService.ensureMana(player);
        if (AbilityService.data(player).mana() < BroomEntity.manaPerSecond(player)) return Result.NO_MANA;
        return Result.OK;
    }

    /** Mounts a new broom; anything unmet denies with a message above the hotbar. */
    public static Result mount(ServerPlayer player) {
        Result result = check(player);
        if (result != Result.OK) {
            if (result != Result.CANNOT_ACT) player.displayClientMessage(Component.translatable(Ids.key("message", "broom." + result.name().toLowerCase(java.util.Locale.ROOT)))
                    .withStyle(ChatFormatting.RED), true);
            return result;
        }
        BroomEntity broom = new BroomEntity(player.level(), player);
        player.level().addFreshEntity(broom);
        player.startRiding(broom, true);
        return result;
    }

    /** One second of flight; false (gliding) when the mana or Mana Control ran out. */
    public static boolean payFlight(ServerPlayer player) {
        if (SkillTreeService.effectRank(player, SkillEffects.MANA_CONTROL) < AbilityConfig.BROOM_MANA_CONTROL.get()) return false;
        return AbilityService.spend(player, BroomEntity.manaPerSecond(player));
    }
}
