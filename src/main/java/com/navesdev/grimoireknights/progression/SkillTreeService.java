package com.navesdev.grimoireknights.progression;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import com.navesdev.grimoireknights.progression.attachment.SkillTreeData;
import com.navesdev.grimoireknights.progression.rules.SkillTreeRules;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Points, buying and refunding nodes, and the effects ability and progression read from the tree. */
public final class SkillTreeService {
    private SkillTreeService() {}

    public static SkillTreeData data(Player player) {
        return player.getData(ProgressionModule.SKILL_TREE_DATA);
    }

    /** Works on both sides: the node registry is synced to clients. */
    public static Optional<Registry<SkillNode>> registry(Player player) {
        return player.level().registryAccess().registry(ProgressionRegistries.SKILL_NODE);
    }

    public static Optional<SkillNode> node(Player player, ResourceLocation id) {
        return registry(player).flatMap(registry -> registry.getOptional(id));
    }

    public static Stream<Map.Entry<ResourceLocation, SkillNode>> nodes(Player player) {
        return registry(player).stream().flatMap(registry -> registry.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey().location(), entry.getValue())));
    }

    /** The mastery whose points pay for this node; empty for a grimoire node without an element. */
    public static Optional<ResourceLocation> mastery(Player player, SkillNode node) {
        return switch (node.branch()) {
            case BODY -> Optional.of(Masteries.BODY);
            case MANA -> Optional.of(MasteryService.resourceMastery(player));
            case GRIMOIRE -> node.element();
        };
    }

    /** Grimoire nodes count only for the current element; Body and Mana always. */
    public static boolean active(Player player, SkillNode node) {
        return node.branch() != SkillNode.Branch.GRIMOIRE || node.element().equals(GrimoireService.data(player).element());
    }

    public static void addPoints(ServerPlayer player, ResourceLocation mastery, int amount) {
        SkillTreeData data = data(player);
        player.setData(ProgressionModule.SKILL_TREE_DATA, data.withPoints(mastery, data.points(mastery) + amount));
    }

    public static void setPoints(ServerPlayer player, ResourceLocation mastery, int amount) {
        player.setData(ProgressionModule.SKILL_TREE_DATA, data(player).withPoints(mastery, amount));
    }

    public static SkillTreeRules.Result check(Player player, ResourceLocation id) {
        Optional<SkillNode> found = node(player, id);
        if (found.isEmpty()) return SkillTreeRules.Result.UNKNOWN_NODE;
        SkillNode node = found.get();
        Optional<ResourceLocation> mastery = mastery(player, node);
        if (mastery.isEmpty() || !active(player, node)) return SkillTreeRules.Result.WRONG_ELEMENT;
        SkillTreeData data = data(player);
        return SkillTreeRules.canBuy(node.rules(), data.rank(id), data.points(mastery.get()), data.nodes(), RankService.rank(player));
    }

    /** Buys the next rank of a node. Anything unmet denies. */
    public static SkillTreeRules.Result buy(ServerPlayer player, ResourceLocation id) {
        SkillTreeRules.Result result = check(player, id);
        if (result != SkillTreeRules.Result.OK) {
            GrimoireKnights.LOGGER.debug("Node {} denied for {}: {}", id, player.getGameProfile().getName(), result);
            return result;
        }
        SkillNode node = node(player, id).orElseThrow();
        ResourceLocation mastery = mastery(player, node).orElseThrow();
        SkillTreeData data = data(player);
        int owned = data.rank(id);
        player.setData(ProgressionModule.SKILL_TREE_DATA, data
                .withPoints(mastery, data.points(mastery) - SkillTreeRules.nextCost(node.rules(), owned))
                .withNode(id, owned + 1));
        return result;
    }

    /** Refunds every node to its mastery's points. Nodes gone from the datapack are dropped. */
    public static void reset(ServerPlayer player) {
        SkillTreeData data = data(player);
        SkillTreeData next = new SkillTreeData(data.points(), Map.of());
        for (Map.Entry<ResourceLocation, Integer> owned : data.nodes().entrySet()) {
            Optional<SkillNode> node = node(player, owned.getKey());
            Optional<ResourceLocation> mastery = node.flatMap(n -> mastery(player, n));
            if (mastery.isPresent()) {
                next = next.withPoints(mastery.get(), next.points(mastery.get()) + SkillTreeRules.refund(node.get().rules(), owned.getValue()));
            }
        }
        player.setData(ProgressionModule.SKILL_TREE_DATA, next);
    }

    private static Stream<Map.Entry<SkillNode, Integer>> owned(Player player) {
        return data(player).nodes().entrySet().stream()
                .flatMap(entry -> node(player, entry.getKey()).stream().map(node -> Map.entry(node, entry.getValue())))
                .filter(entry -> active(player, entry.getKey()));
    }

    /** Sum of the ranks of every active node with this effect type. */
    public static int effectRank(Player player, ResourceLocation type) {
        return owned(player).filter(entry -> entry.getKey().effect().type().equals(type)).mapToInt(Map.Entry::getValue).sum();
    }

    /** Highest upgrade step bought for a spell (0 = none). */
    public static int upgradeLevel(Player player, ResourceLocation ability) {
        return owned(player).map(Map.Entry::getKey)
                .filter(node -> node.effect().type().equals(SkillEffects.SPELL_UPGRADE) && node.effect().ability().equals(Optional.of(ability)))
                .mapToInt(node -> node.effect().level()).max().orElse(0);
    }

    /** Whether the passive of the current element is bought. */
    public static boolean hasPassive(Player player) {
        return effectRank(player, SkillEffects.PASSIVE) > 0;
    }
}
