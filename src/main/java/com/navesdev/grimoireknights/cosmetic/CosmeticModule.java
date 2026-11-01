package com.navesdev.grimoireknights.cosmetic;

import com.mojang.serialization.Codec;
import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.network.Network;
import com.navesdev.grimoireknights.core.registry.CoreTabs;
import com.navesdev.grimoireknights.cosmetic.network.ArmorLookPayload;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Armor toggle and cosmetic accessories (Curios slots {@code cosmetic_head|chest|legs|feet}). */
public final class CosmeticModule {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, GrimoireKnights.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GrimoireKnights.MODID);

    /** Everyone who sees the player draws it, so it goes to all of them. */
    public static final Supplier<AttachmentType<Boolean>> ARMOR_HIDDEN = ATTACHMENTS.register("armor_look", () -> AttachmentType
            .builder(() -> false)
            .serialize(Codec.BOOL)
            .copyOnDeath()
            .sync((holder, to) -> true, ByteBufCodecs.BOOL)
            .build());

    public static final Map<Outfit, Map<EquipmentSlot, DeferredItem<Item>>> OUTFITS = new EnumMap<>(Outfit.class);

    static {
        for (Outfit outfit : Outfit.values()) {
            Map<EquipmentSlot, DeferredItem<Item>> pieces = new EnumMap<>(EquipmentSlot.class);
            outfit.pieces.forEach((slot, name) -> pieces.put(slot, ITEMS.registerSimpleItem(outfit.id + "_" + name, new Item.Properties().stacksTo(1))));
            OUTFITS.put(outfit, pieces);
        }
    }

    private CosmeticModule() {}

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, event -> {
            if (event.getTabKey() != CoreTabs.MAIN) return;
            OUTFITS.values().forEach(pieces -> pieces.values().forEach(event::accept));
        });
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> Network.registrar(event)
                .playToServer(ArmorLookPayload.TYPE, ArmorLookPayload.STREAM_CODEC, (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) ArmorLook.setArmorHidden(player, payload.hidden());
                }));
    }
}
