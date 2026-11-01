package com.navesdev.grimoireknights.cosmetic;

import java.util.Map;
import net.minecraft.world.entity.EquipmentSlot;

/** A cosmetic outfit: one accessory per armor slot, all drawn from {@code textures/models/outfit/<id>_layer_1|2.png}. */
public enum Outfit {
    MAGIC_KNIGHT("magic_knight", Map.of(EquipmentSlot.HEAD, "hood", EquipmentSlot.CHEST, "robe", EquipmentSlot.LEGS, "trousers", EquipmentSlot.FEET, "boots"));

    public final String id;
    /** Item name suffix of each piece. */
    public final Map<EquipmentSlot, String> pieces;

    Outfit(String id, Map<EquipmentSlot, String> pieces) {
        this.id = id;
        this.pieces = pieces;
    }
}
