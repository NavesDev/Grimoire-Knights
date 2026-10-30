package com.navesdev.grimoireknights.grimoire;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.grimoire.attachment.GrimoireData;
import com.navesdev.grimoireknights.grimoire.config.GrimoireConfig;
import com.navesdev.grimoireknights.grimoire.event.GrimoireCalledEvent;
import com.navesdev.grimoireknights.grimoire.rules.ElementLottery;
import com.navesdev.grimoireknights.grimoire.rules.GrimoireCalling;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

/** Grimoire data of a player: lottery, giving and taking the grimoire, the calling bar. */
public final class GrimoireService {
    /** Elementals always come with 3 leaves from the ceremony. */
    public static final int CEREMONY_LEAVES = 3;

    private GrimoireService() {}

    public static GrimoireData data(Player player) {
        return player.getData(GrimoireModule.GRIMOIRE_DATA);
    }

    public static Optional<Element> element(Player player) {
        return data(player).element().flatMap(GrimoireRegistries.ELEMENTS::getOptional);
    }

    /** Lottery weights from the config, elements in id order; the empty key is "no magic". */
    public static Map<Optional<ResourceLocation>, Integer> lotteryWeights() {
        Map<Optional<ResourceLocation>, Integer> weights = new LinkedHashMap<>();
        GrimoireRegistries.ELEMENTS.keySet().stream().sorted()
                .forEach(id -> weights.put(Optional.of(id), GrimoireConfig.ELEMENTAL_WEIGHT.get()));
        weights.put(Optional.empty(), GrimoireConfig.NO_MAGIC_WEIGHT.get());
        return weights;
    }

    public static Component elementName(ResourceLocation element) {
        return Component.translatable("element." + element.getNamespace() + "." + element.getPath());
    }

    public static Optional<ResourceLocation> drawElement(RandomSource random) {
        return ElementLottery.draw(lotteryWeights(), random::nextInt);
    }

    public static void give(ServerPlayer player, ResourceLocation element, int leaves) {
        player.setData(GrimoireModule.GRIMOIRE_DATA, data(player).withGrimoire(element, leaves));
    }

    public static void clear(ServerPlayer player) {
        player.setData(GrimoireModule.GRIMOIRE_DATA, data(player).cleared());
    }

    public static double callingMax() {
        return GrimoireConfig.CALLING_MAX.get();
    }

    public static boolean callingFull(Player player) {
        return GrimoireCalling.isFull(data(player).calling(), callingMax());
    }

    /** Health dealt to or taken from a hostile mob fills the calling, until the ceremony. */
    public static void addCallingDamage(ServerPlayer player, float damage) {
        GrimoireData data = data(player);
        if (data.ceremonyDone()) return;
        double points = GrimoireCalling.points(damage, GrimoireConfig.CALLING_HEALTH_PER_POINT.get());
        setCalling(player, GrimoireCalling.add(data.calling(), points, callingMax()));
    }

    public static void setCalling(ServerPlayer player, double value) {
        GrimoireData data = data(player);
        double next = Math.max(0, Math.min(callingMax(), value));
        if (next == data.calling()) return;
        player.setData(GrimoireModule.GRIMOIRE_DATA, data.withCalling(next));
        if (!data.ceremonyDone() && GrimoireCalling.justFilled(data.calling(), next, callingMax())) {
            call(player);
        }
    }

    /** Tells the player the tower calls them; {@code world} points the way. */
    public static void call(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(Ids.key("message", "calling.filled")));
        NeoForge.EVENT_BUS.post(new GrimoireCalledEvent(player));
    }
}
