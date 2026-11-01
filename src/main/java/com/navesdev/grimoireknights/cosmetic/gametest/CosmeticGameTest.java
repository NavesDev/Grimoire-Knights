package com.navesdev.grimoireknights.cosmetic.gametest;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.gametest.TestPlayers;
import com.navesdev.grimoireknights.cosmetic.ArmorLook;
import com.navesdev.grimoireknights.cosmetic.CosmeticModule;
import com.navesdev.grimoireknights.cosmetic.Outfit;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;

@GameTestHolder(GrimoireKnights.MODID)
@PrefixGameTestTemplate(false)
public class CosmeticGameTest {
    @GameTest(template = "empty")
    public static void toggleHidesEveryPiece(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        helper.assertTrue(!ArmorLook.hidden(player, EquipmentSlot.CHEST), "shown by default");
        ArmorLook.setArmorHidden(player, true);
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            helper.assertTrue(ArmorLook.hidden(player, slot), slot + " hidden");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void anAccessoryCoversOnlyItsPiece(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        ItemStack hood = new ItemStack(CosmeticModule.OUTFITS.get(Outfit.MAGIC_KNIGHT).get(EquipmentSlot.HEAD).get());
        CuriosApi.getCuriosInventory(player).orElseThrow().setEquippedCurio(ArmorLook.slot(EquipmentSlot.HEAD), 0, hood);
        helper.assertTrue(ArmorLook.hidden(player, EquipmentSlot.HEAD), "the hood covers the helmet");
        helper.assertTrue(!ArmorLook.hidden(player, EquipmentSlot.CHEST), "the chestplate still shows");
        helper.succeed();
    }
}
