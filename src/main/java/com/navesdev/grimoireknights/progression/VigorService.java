package com.navesdev.grimoireknights.progression;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.progression.config.ProgressionConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Vigor nodes add max health through one attribute modifier, kept in step with the tree. */
public final class VigorService {
    private static final ResourceLocation MODIFIER = Ids.id("vigor");

    private VigorService() {}

    /** Permanent, so the extra health survives the save; dropping it clamps the health to the new max. */
    public static void refresh(ServerPlayer player) {
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) return;
        double amount = SkillTreeService.effectRank(player, SkillEffects.VIGOR) * ProgressionConfig.VIGOR_HEALTH_PER_RANK.get();
        AttributeModifier current = health.getModifier(MODIFIER);
        if (current != null && current.amount() == amount) return;
        if (amount <= 0) health.removeModifier(MODIFIER);
        else health.addOrReplacePermanentModifier(new AttributeModifier(MODIFIER, amount, AttributeModifier.Operation.ADD_VALUE));
    }
}
