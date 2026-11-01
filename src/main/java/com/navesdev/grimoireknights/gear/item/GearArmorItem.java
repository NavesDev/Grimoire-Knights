package com.navesdev.grimoireknights.gear.item;

import com.google.common.base.Suppliers;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.progression.ProgressionModule;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/** A mod armor piece: the vanilla modifiers plus its spell resistance. */
public class GearArmorItem extends ArmorItem {
    private final Supplier<ItemAttributeModifiers> modifiers;

    public GearArmorItem(Holder<ArmorMaterial> material, Type type, double spellResistance, Properties properties) {
        super(material, type, properties);
        this.modifiers = Suppliers.memoize(() -> super.getDefaultAttributeModifiers().withModifierAdded(ProgressionModule.SPELL_RESISTANCE,
                new AttributeModifier(Ids.id("armor." + type.getName() + ".spell_resistance"), spellResistance, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.bySlot(type.getSlot())));
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return modifiers.get();
    }
}
