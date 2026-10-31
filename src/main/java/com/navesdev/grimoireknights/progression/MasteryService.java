package com.navesdev.grimoireknights.progression;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import com.navesdev.grimoireknights.progression.attachment.MasteryData;
import com.navesdev.grimoireknights.progression.config.ProgressionConfig;
import com.navesdev.grimoireknights.progression.rules.MasteryRules;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Mastery levels and gains on the server. Each level gained gives 1 point to that mastery's branch. */
public final class MasteryService {
    private static final Set<ResourceLocation> NON_ELEMENT = Set.of(Masteries.BODY, Masteries.MANA, Masteries.ANTIMANA);

    private MasteryService() {}

    public static MasteryData data(Player player) {
        return player.getData(ProgressionModule.MASTERY_DATA);
    }

    public static MasteryData.Entry get(Player player, ResourceLocation mastery) {
        return data(player).get(mastery);
    }

    public static boolean isElement(ResourceLocation mastery) {
        return !NON_ELEMENT.contains(mastery);
    }

    public static MasteryRules.Curve curve() {
        return new MasteryRules.Curve(ProgressionConfig.CURVE_BASE.get(), ProgressionConfig.CURVE_MULT.get(),
                ProgressionConfig.CURVE_EXP.get(), ProgressionConfig.MAX_LEVEL.get());
    }

    /** The grimoire mastery stops at the rank ceiling; Body and Mana only at the max level. */
    public static int ceiling(Player player, ResourceLocation mastery) {
        return isElement(mastery) ? Math.min(RankService.ceiling(player), ProgressionConfig.MAX_LEVEL.get()) : ProgressionConfig.MAX_LEVEL.get();
    }

    /** The grimoire mastery of the player's current element; empty without a grimoire. */
    public static Optional<ResourceLocation> elementMastery(Player player) {
        return GrimoireService.data(player).element();
    }

    public static ResourceLocation resourceMastery(Player player) {
        return Masteries.resource(elementMastery(player));
    }

    public static void add(ServerPlayer player, ResourceLocation mastery, double amount) {
        if (amount <= 0) return;
        MasteryData.Entry entry = get(player, mastery);
        int ceiling = ceiling(player, mastery);
        MasteryRules.Progress result = MasteryRules.add(entry.level(), entry.progress(), amount, ceiling, curve());
        if (result.level() == entry.level() && result.progress() == entry.progress()) return;
        player.setData(ProgressionModule.MASTERY_DATA, data(player).with(mastery, new MasteryData.Entry(result.level(), result.progress())));
        if (result.levelsGained() > 0) levelledUp(player, mastery, result.level(), result.levelsGained(), ceiling);
    }

    /** Test command: sets the level with no progress; levels above the old one give their points. */
    public static void set(ServerPlayer player, ResourceLocation mastery, int level) {
        int old = get(player, mastery).level();
        int next = Math.max(1, Math.min(level, ProgressionConfig.MAX_LEVEL.get()));
        player.setData(ProgressionModule.MASTERY_DATA, data(player).with(mastery, new MasteryData.Entry(next, 0)));
        if (next > old) SkillTreeService.addPoints(player, mastery, next - old);
    }

    /** Mastery a hit on or a kill of this entity is worth: the data map, or max health times the config ratio. */
    public static double enemyValue(LivingEntity entity) {
        MasteryValue value = entity.getType().builtInRegistryHolder().getData(ProgressionRegistries.MASTERY);
        return value != null ? value.amount() : entity.getMaxHealth() * ProgressionConfig.DEFAULT_VALUE_PER_HEALTH.get();
    }

    public static double strengthFactor(Player player, ResourceLocation mastery, LivingEntity enemy) {
        return MasteryRules.strengthFactor(enemyValue(enemy), get(player, mastery).level(),
                ProgressionConfig.FACTOR_PER_LEVEL.get(), ProgressionConfig.FACTOR_EXP.get());
    }

    /** Damage dealt or taken, scaled by the enemy strength factor. */
    public static void addFromDamage(ServerPlayer player, ResourceLocation mastery, LivingEntity enemy, double damage, double ratio) {
        add(player, mastery, damage * ratio * strengthFactor(player, mastery, enemy));
    }

    public static void addFromKill(ServerPlayer player, ResourceLocation mastery, LivingEntity enemy) {
        add(player, mastery, enemyValue(enemy) * strengthFactor(player, mastery, enemy));
    }

    public static Component name(ResourceLocation mastery) {
        return isElement(mastery) ? GrimoireService.elementName(mastery)
                : Component.translatable("mastery." + mastery.getNamespace() + "." + mastery.getPath());
    }

    /** Discreet notice above the hotbar (approved mockup): level and points, or the rank ceiling when reached. */
    private static void levelledUp(ServerPlayer player, ResourceLocation mastery, int level, int gained, int ceiling) {
        SkillTreeService.addPoints(player, mastery, gained);
        boolean capped = isElement(mastery) && level >= ceiling && ceiling < ProgressionConfig.MAX_LEVEL.get();
        Component notice = capped
                ? Component.translatable(Ids.key("message", "mastery.ceiling"), name(mastery), level)
                : Component.translatable(Ids.key("message", "mastery.level_up"), name(mastery), level, gained);
        player.displayClientMessage(notice.copy().withStyle(capped ? ChatFormatting.RED : ChatFormatting.GOLD), true);
    }
}
