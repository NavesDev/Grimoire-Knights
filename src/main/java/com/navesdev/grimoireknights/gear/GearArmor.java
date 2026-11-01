package com.navesdev.grimoireknights.gear;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.ArmorItem;

/**
 * Stats of the mod's armors. Item data fixed at registration like vanilla materials, not server config: client and
 * server must agree before any config loads. Netherite: 20 defense, 3 toughness, 0.1 knockback, no spell resistance.
 */
public enum GearArmor {
    KNIGHT("knight", 2, 7, 5, 2, 1.0f, 0.0f, 12, 20, 0.05),
    ELITE_KNIGHT("elite_knight", 3, 8, 6, 3, 2.5f, 0.05f, 14, 35, 0.08);

    public final String id;
    public final Map<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
    public final float toughness;
    public final float knockbackResistance;
    public final int enchantability;
    public final int durabilityFactor;
    /** Spell resistance of each piece; the full set gives four times this. */
    public final double spellResistance;

    GearArmor(String id, int helmet, int chest, int legs, int boots, float toughness, float knockbackResistance,
              int enchantability, int durabilityFactor, double spellResistance) {
        this.id = id;
        defense.put(ArmorItem.Type.HELMET, helmet);
        defense.put(ArmorItem.Type.CHESTPLATE, chest);
        defense.put(ArmorItem.Type.LEGGINGS, legs);
        defense.put(ArmorItem.Type.BOOTS, boots);
        defense.put(ArmorItem.Type.BODY, chest);
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.enchantability = enchantability;
        this.durabilityFactor = durabilityFactor;
        this.spellResistance = spellResistance;
    }
}
