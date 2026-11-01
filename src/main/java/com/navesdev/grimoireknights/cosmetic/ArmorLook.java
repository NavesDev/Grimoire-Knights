package com.navesdev.grimoireknights.cosmetic;

import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/** What the player's armor looks like: hidden by the toggle, or covered by the accessory in the same slot. Defense never changes. */
public final class ArmorLook {
    private ArmorLook() {}

    /** Curios slot of the accessory that covers this armor piece. */
    public static String slot(EquipmentSlot armor) {
        return "cosmetic_" + switch (armor) {
            case HEAD -> "head";
            case CHEST -> "chest";
            case LEGS -> "legs";
            default -> "feet";
        };
    }

    public static boolean armorHidden(Player player) {
        return player.getData(CosmeticModule.ARMOR_HIDDEN);
    }

    public static void setArmorHidden(ServerPlayer player, boolean hidden) {
        player.setData(CosmeticModule.ARMOR_HIDDEN, hidden);
    }

    /** True when this piece is not drawn: the toggle is on, or a shown accessory sits in its slot. */
    public static boolean hidden(Player player, EquipmentSlot armor) {
        return armorHidden(player) || covered(player, armor);
    }

    private static boolean covered(Player player, EquipmentSlot armor) {
        Optional<ICurioStacksHandler> handler = CuriosApi.getCuriosInventory(player).flatMap(inventory -> inventory.getStacksHandler(slot(armor)));
        if (handler.isEmpty()) return false;
        for (int i = 0; i < handler.get().getSlots(); i++) {
            if (!handler.get().getStacks().getStackInSlot(i).isEmpty() && handler.get().getRenders().get(i)) return true;
        }
        return false;
    }
}
