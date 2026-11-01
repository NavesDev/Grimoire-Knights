package com.navesdev.grimoireknights.progression;

import com.navesdev.grimoireknights.core.Ids;
import net.minecraft.resources.ResourceLocation;

/** Node effect types; each is applied where its feature reads it. */
public final class SkillEffects {
    public static final ResourceLocation DASH = Ids.id("dash");
    public static final ResourceLocation FALL_RESISTANCE = Ids.id("fall_resistance");
    public static final ResourceLocation ROLL = Ids.id("roll");
    public static final ResourceLocation VIGOR = Ids.id("vigor");
    public static final ResourceLocation MAX_MANA = Ids.id("max_mana");
    public static final ResourceLocation MANA_REGEN = Ids.id("mana_regen");
    public static final ResourceLocation MANA_CONTROL = Ids.id("mana_control");
    public static final ResourceLocation SPELL_UPGRADE = Ids.id("spell_upgrade");
    public static final ResourceLocation PASSIVE = Ids.id("passive");

    private SkillEffects() {}
}
