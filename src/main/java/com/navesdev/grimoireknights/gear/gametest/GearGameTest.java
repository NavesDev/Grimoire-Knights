package com.navesdev.grimoireknights.gear.gametest;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.gear.GearArmor;
import com.navesdev.grimoireknights.gear.GearModule;
import com.navesdev.grimoireknights.progression.ProgressionModule;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GrimoireKnights.MODID)
@PrefixGameTestTemplate(false)
public class GearGameTest {
    @GameTest(template = "empty")
    public static void eliteSetGivesDiamondDefenseAndSpellResistance(GameTestHelper helper) {
        Mob zombie = helper.spawn(EntityType.ZOMBIE, 1, 2, 1);
        zombie.getAttribute(Attributes.ARMOR).setBaseValue(0);
        GearModule.ARMOR.get(GearArmor.ELITE_KNIGHT).forEach((type, item) -> zombie.setItemSlot(type.getSlot(), new ItemStack(item.get())));
        helper.succeedWhen(() -> {
            helper.assertTrue(zombie.getAttributeValue(Attributes.ARMOR) == 20, "defense " + zombie.getAttributeValue(Attributes.ARMOR));
            helper.assertTrue(Math.abs(zombie.getAttributeValue(ProgressionModule.SPELL_RESISTANCE) - 0.32) < 1e-6,
                    "spell resistance " + zombie.getAttributeValue(ProgressionModule.SPELL_RESISTANCE));
        });
    }
}
