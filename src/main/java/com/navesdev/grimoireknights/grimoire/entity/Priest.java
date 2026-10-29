package com.navesdev.grimoireknights.grimoire.entity;

import com.navesdev.grimoireknights.grimoire.CeremonyService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Village priest: explains the grimoire calling and, next to a Grimoire Altar, holds the ceremony.
 * A cleric that does not trade; it cannot be hurt (only /kill), so the ceremony is never lost.
 */
public class Priest extends Villager {
    public Priest(EntityType<? extends Priest> type, Level level) {
        super(type, level);
        setVillagerData(getVillagerData().setProfession(VillagerProfession.CLERIC).setLevel(5));
        // Experience keeps the profession without a job site.
        setVillagerXp(1);
        setPersistenceRequired();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (player instanceof ServerPlayer serverPlayer) {
            CeremonyService.talk(serverPlayer, this);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    @Override
    public boolean canBreed() {
        return false;
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt lightning) {
        // stays a priest
    }
}
