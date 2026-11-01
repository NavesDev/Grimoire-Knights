package com.navesdev.grimoireknights.gear;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.registry.CoreTabs;
import com.navesdev.grimoireknights.gear.item.GearArmorItem;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The mod's armors (Knight, Elite Knight) and the mana steel they are made of. */
public final class GearModule {
    private static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, GrimoireKnights.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GrimoireKnights.MODID);
    private static final List<ArmorItem.Type> PIECES = List.of(ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS, ArmorItem.Type.BOOTS);

    public static final DeferredItem<Item> MANA_STEEL_INGOT = ITEMS.registerSimpleItem("mana_steel_ingot", new Item.Properties());
    public static final Map<GearArmor, Map<ArmorItem.Type, DeferredItem<GearArmorItem>>> ARMOR = new EnumMap<>(GearArmor.class);

    static {
        for (GearArmor armor : GearArmor.values()) {
            Holder<ArmorMaterial> material = MATERIALS.register(armor.id, () -> new ArmorMaterial(armor.defense, armor.enchantability,
                    armor == GearArmor.KNIGHT ? SoundEvents.ARMOR_EQUIP_IRON : SoundEvents.ARMOR_EQUIP_DIAMOND,
                    () -> armor == GearArmor.KNIGHT ? Ingredient.of(MANA_STEEL_INGOT.get()) : Ingredient.of(Items.DIAMOND),
                    List.of(new ArmorMaterial.Layer(Ids.id(armor.id))), armor.toughness, armor.knockbackResistance));
            Map<ArmorItem.Type, DeferredItem<GearArmorItem>> pieces = new EnumMap<>(ArmorItem.Type.class);
            for (ArmorItem.Type type : PIECES) {
                pieces.put(type, ITEMS.register(armor.id + "_" + type.getName(), () -> new GearArmorItem(material, type, armor.spellResistance,
                        new Item.Properties().durability(type.getDurability(armor.durabilityFactor)))));
            }
            ARMOR.put(armor, pieces);
        }
    }

    private GearModule() {}

    public static void register(IEventBus modBus) {
        MATERIALS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, event -> {
            if (event.getTabKey() != CoreTabs.MAIN) return;
            event.accept(MANA_STEEL_INGOT);
            ARMOR.values().forEach(pieces -> PIECES.forEach(type -> event.accept(pieces.get(type))));
        });
    }
}
