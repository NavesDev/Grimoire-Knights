package com.navesdev.grimoireknights.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.navesdev.grimoireknights.progression.attachment.RankData;
import com.navesdev.grimoireknights.progression.rules.Rank;
import com.navesdev.grimoireknights.progression.rules.SkillTreeRules;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

/**
 * A skill tree node from the {@code grimoireknights:skill_node} datapack registry. The effect is applied in code by its type;
 * {@code icon} (an item) and {@code x}/{@code y} (grid cell, row 0 at the bottom) only draw the tree screen.
 */
public record SkillNode(Branch branch, Optional<ResourceLocation> element, List<Integer> cost, List<ResourceLocation> requires,
                        Rank requiredRank, Effect effect, ResourceLocation icon, int x, int y) {
    public static final Codec<SkillNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Branch.CODEC.fieldOf("branch").forGetter(SkillNode::branch),
            ResourceLocation.CODEC.optionalFieldOf("element").forGetter(SkillNode::element),
            ExtraCodecs.POSITIVE_INT.listOf(1, 10).fieldOf("cost").forGetter(SkillNode::cost),
            ResourceLocation.CODEC.listOf().optionalFieldOf("requires", List.of()).forGetter(SkillNode::requires),
            RankData.RANK_CODEC.optionalFieldOf("required_rank", Rank.RECRUIT).forGetter(SkillNode::requiredRank),
            Effect.CODEC.fieldOf("effect").forGetter(SkillNode::effect),
            ResourceLocation.CODEC.fieldOf("icon").forGetter(SkillNode::icon),
            Codec.intRange(0, 15).fieldOf("x").forGetter(SkillNode::x),
            Codec.intRange(0, 15).fieldOf("y").forGetter(SkillNode::y)
    ).apply(instance, SkillNode::new));

    public enum Branch implements StringRepresentable {
        BODY("body"),
        MANA("mana"),
        GRIMOIRE("grimoire");

        public static final Codec<Branch> CODEC = StringRepresentable.fromEnum(Branch::values);
        private final String name;

        Branch(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    /** @param ability the spell an upgrade improves; @param level the upgrade's step (1, 2) */
    public record Effect(ResourceLocation type, Optional<ResourceLocation> ability, int level) {
        public static final Codec<Effect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("type").forGetter(Effect::type),
                ResourceLocation.CODEC.optionalFieldOf("ability").forGetter(Effect::ability),
                Codec.INT.optionalFieldOf("level", 1).forGetter(Effect::level)
        ).apply(instance, Effect::new));
    }

    public int maxRank() {
        return cost.size();
    }

    public SkillTreeRules.Node<ResourceLocation> rules() {
        return new SkillTreeRules.Node<>(cost, requires, requiredRank);
    }
}
