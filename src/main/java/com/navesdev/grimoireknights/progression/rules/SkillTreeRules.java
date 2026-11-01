package com.navesdev.grimoireknights.progression.rules;

import java.util.List;
import java.util.Map;

/** Buying and refunding skill tree nodes. Any unmet condition denies. */
public final class SkillTreeRules {
    public enum Result { OK, UNKNOWN_NODE, MAXED, NO_POINTS, MISSING_PREREQUISITE, RANK_TOO_LOW, WRONG_ELEMENT }

    /** @param cost price of each rank; its size is the max rank */
    public record Node<K>(List<Integer> cost, List<K> prerequisites, Rank requiredRank) {
        public Node {
            if (cost.isEmpty()) throw new IllegalArgumentException("a node needs at least one rank");
            cost = List.copyOf(cost);
            prerequisites = List.copyOf(prerequisites);
        }

        public int maxRank() {
            return cost.size();
        }
    }

    private SkillTreeRules() {}

    public static <K> Result canBuy(Node<K> node, int ownedRank, int points, Map<K, Integer> owned, Rank playerRank) {
        if (ownedRank >= node.maxRank()) return Result.MAXED;
        if (!playerRank.atLeast(node.requiredRank())) return Result.RANK_TOO_LOW;
        if (!node.prerequisites().stream().allMatch(id -> owned.getOrDefault(id, 0) > 0)) return Result.MISSING_PREREQUISITE;
        if (points < nextCost(node, ownedRank)) return Result.NO_POINTS;
        return Result.OK;
    }

    /** The tree only shows what is bought or next in line (its prerequisites bought), so the rest stays a mystery. */
    public static <K> boolean revealed(Node<K> node, int ownedRank, Map<K, Integer> owned) {
        return ownedRank > 0 || node.prerequisites().stream().allMatch(id -> owned.getOrDefault(id, 0) > 0);
    }

    public static int nextCost(Node<?> node, int ownedRank) {
        return node.cost().get(ownedRank);
    }

    /** Points spent on the first {@code ownedRank} ranks. */
    public static int refund(Node<?> node, int ownedRank) {
        return node.cost().subList(0, Math.min(ownedRank, node.maxRank())).stream().mapToInt(Integer::intValue).sum();
    }
}
