package com.navesdev.grimoireknights.grimoire;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.grimoire.block.GrimoireAltarBlockEntity;
import com.navesdev.grimoireknights.grimoire.config.GrimoireConfig;
import com.navesdev.grimoireknights.grimoire.entity.Priest;
import com.navesdev.grimoireknights.grimoire.network.CeremonyPayload;
import com.navesdev.grimoireknights.grimoire.network.ElementChoicePayload;
import com.navesdev.grimoireknights.quest.QuestService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The first-grimoire ceremony, once per player: the priest holds it at a Grimoire Altar after the calling is full.
 * A grimoire comes down from the shelves and chooses the player; with no magic, the forgotten grimoire quest starts instead.
 */
public final class CeremonyService {
    public static final ResourceLocation FORGOTTEN_GRIMOIRE_QUEST = Ids.id("forgotten_grimoire");
    /** The grimoire comes down from this high above the altar (the tower's shelves). */
    private static final int SHELF_HEIGHT = 8;
    /** The grimoire reaches the player this long after the ceremony (flight and {@code choose} on the client); the achievement waits for it. */
    public static final int REVEAL_TICKS = 80;

    public enum Result { GRIMOIRE, NO_MAGIC, CHOOSING, ALREADY_DONE, FAILED }

    /** Free choice waiting for the player's pick: player to altar (null when held without an altar). */
    private static final Map<UUID, Optional<BlockPos>> PENDING_CHOICE = new HashMap<>();
    /** Game time when the player's grimoire achievement is due. */
    private static final Map<UUID, Long> PENDING_REVEAL = new HashMap<>();

    private CeremonyService() {}

    /** Right-click on the priest. */
    public static void talk(ServerPlayer player, Priest priest) {
        if (GrimoireService.data(player).ceremonyDone()) {
            say(player, "priest.done");
        } else if (!GrimoireService.callingFull(player)) {
            say(player, "priest.calling");
        } else {
            Optional<BlockPos> altar = findAltar(player.serverLevel(), priest.home(), GrimoireConfig.PRIEST_ALTAR_RADIUS.get())
                    .or(() -> findAltar(player.serverLevel(), priest.blockPosition(), GrimoireConfig.PRIEST_ALTAR_RADIUS.get()));
            if (altar.isEmpty()) {
                say(player, "priest.go_to_tower");
            } else {
                say(player, "priest.begin");
                start(player, altar.get());
            }
        }
    }

    /** Runs the ceremony; {@code altar} null runs it where the player stands (test command). */
    public static Result start(ServerPlayer player, @Nullable BlockPos altar) {
        if (GrimoireService.data(player).ceremonyDone()) return Result.ALREADY_DONE;
        List<ResourceLocation> elements = GrimoireRegistries.ELEMENTS.keySet().stream().sorted().toList();
        if (player.serverLevel().getGameRules().getBoolean(GrimoireGameRules.FREE_ELEMENT_CHOICE) && !elements.isEmpty()) {
            PENDING_CHOICE.put(player.getUUID(), Optional.ofNullable(altar));
            PacketDistributor.sendToPlayer(player, new ElementChoicePayload(elements));
            return Result.CHOOSING;
        }
        return finish(player, altar, GrimoireService.drawElement(player.getRandom()));
    }

    /** The player's free-choice pick. Denied unless a choice is pending and the element exists. */
    public static boolean choose(ServerPlayer player, ResourceLocation element) {
        Optional<BlockPos> altar = PENDING_CHOICE.get(player.getUUID());
        if (altar == null || !GrimoireRegistries.ELEMENTS.containsKey(element) || GrimoireService.data(player).ceremonyDone()) {
            GrimoireKnights.LOGGER.debug("Denied element choice {} from {}", element, player.getGameProfile().getName());
            return false;
        }
        PENDING_CHOICE.remove(player.getUUID());
        finish(player, altar.orElse(null), Optional.of(element));
        return true;
    }

    static void forget(ServerPlayer player) {
        PENDING_CHOICE.remove(player.getUUID());
    }

    private static Result finish(ServerPlayer player, @Nullable BlockPos altar, Optional<ResourceLocation> element) {
        if (altar != null && player.serverLevel().getBlockEntity(altar) instanceof GrimoireAltarBlockEntity altarEntity) {
            altarEntity.activate();
        }
        if (element.isEmpty()) {
            // Quest first: without it, a player with no magic would be stuck forever.
            QuestService.GiveResult quest = QuestService.give(player, FORGOTTEN_GRIMOIRE_QUEST);
            if (quest != QuestService.GiveResult.OK && quest != QuestService.GiveResult.ALREADY_ACTIVE) {
                GrimoireKnights.LOGGER.error("Forgotten grimoire quest not given to {}: {}", player.getGameProfile().getName(), quest);
                say(player, "ceremony.failed");
                return Result.FAILED;
            }
            player.setData(GrimoireModule.GRIMOIRE_DATA, GrimoireService.data(player).withoutMagic());
            say(player, "ceremony.no_magic");
            return Result.NO_MAGIC;
        }
        GrimoireService.give(player, element.get(), GrimoireService.CEREMONY_LEAVES);
        PENDING_REVEAL.put(player.getUUID(), player.serverLevel().getGameTime() + REVEAL_TICKS);
        BlockPos from = (altar != null ? altar : player.blockPosition()).above(SHELF_HEIGHT);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new CeremonyPayload(player.getId(), from, element.get(), GrimoireService.CEREMONY_LEAVES));
        return Result.GRIMOIRE;
    }

    public static void tickReveal(ServerPlayer player) {
        Long due = PENDING_REVEAL.get(player.getUUID());
        if (due != null && player.serverLevel().getGameTime() >= due) revealNow(player);
    }

    /** Grants the achievement of the player's grimoire (a toast with its picture); leaving early still grants it. */
    public static void revealNow(ServerPlayer player) {
        if (PENDING_REVEAL.remove(player.getUUID()) == null) return;
        GrimoireService.data(player).element().ifPresent(element -> {
            award(player, Ids.id("grimoire/root"));
            award(player, Ids.id("grimoire/chosen_" + element.getPath()));
        });
    }

    private static void award(ServerPlayer player, ResourceLocation id) {
        AdvancementHolder advancement = player.server.getAdvancements().get(id);
        if (advancement == null) {
            GrimoireKnights.LOGGER.warn("Advancement {} missing; no toast for {}", id, player.getGameProfile().getName());
            return;
        }
        advancement.value().criteria().keySet().forEach(criterion -> player.getAdvancements().award(advancement, criterion));
    }

    private static Optional<BlockPos> findAltar(ServerLevel level, BlockPos center, int radius) {
        return BlockPos.findClosestMatch(center, radius, radius / 2 + 2,
                pos -> level.getBlockState(pos).is(GrimoireModule.ALTAR.get()));
    }

    private static void say(ServerPlayer player, String key) {
        player.sendSystemMessage(Component.translatable(Ids.key("message", key)));
    }
}
