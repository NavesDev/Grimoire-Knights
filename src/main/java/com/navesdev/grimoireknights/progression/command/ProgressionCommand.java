package com.navesdev.grimoireknights.progression.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.grimoire.GrimoireRegistries;
import com.navesdev.grimoireknights.progression.Masteries;
import com.navesdev.grimoireknights.progression.MasteryService;
import com.navesdev.grimoireknights.progression.RankService;
import com.navesdev.grimoireknights.progression.SkillTreeService;
import com.navesdev.grimoireknights.progression.attachment.MasteryData;
import com.navesdev.grimoireknights.progression.attachment.SkillTreeData;
import com.navesdev.grimoireknights.progression.rules.Rank;
import com.navesdev.grimoireknights.progression.rules.SkillTreeRules;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** {@code /gk mastery ...}, {@code /gk tree ...} and {@code /gk rank ...}. */
public final class ProgressionCommand {
    /** {@code body}, {@code mana}, {@code antimana}, {@code grimoire} (the current element) or an element id. */
    private static final SuggestionProvider<CommandSourceStack> MASTERIES = (context, builder) -> SharedSuggestionProvider.suggest(
            Stream.concat(Stream.of("body", "mana", "antimana", "grimoire"),
                    Stream.concat(GrimoireRegistries.ELEMENTS.keySet().stream(), Stream.of(Masteries.ANTI_MAGIC)).map(ResourceLocation::toString)),
            builder);
    private static final SuggestionProvider<CommandSourceStack> BRANCHES = (context, builder) -> SharedSuggestionProvider.suggest(new String[] {"body", "mana", "grimoire"}, builder);
    private static final SuggestionProvider<CommandSourceStack> RANKS = (context, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(Rank.values()).map(Rank::id), builder);
    private static final SuggestionProvider<CommandSourceStack> NODES = (context, builder) -> SharedSuggestionProvider.suggestResource(
            context.getSource().getServer().registryAccess().registry(com.navesdev.grimoireknights.progression.ProgressionRegistries.SKILL_NODE)
                    .map(registry -> registry.keySet().stream()).orElse(Stream.empty()), builder);

    private ProgressionCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> mastery() {
        return Commands.literal("mastery")
                .then(Commands.literal("info")
                        .executes(context -> masteryInfo(context, context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player()).executes(context -> masteryInfo(context, EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("set").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("mastery", StringArgumentType.string()).suggests(MASTERIES)
                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 1000)).executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                    Optional<ResourceLocation> mastery = mastery(context, player);
                                    if (mastery.isEmpty()) return 0;
                                    MasteryService.set(player, mastery.get(), IntegerArgumentType.getInteger(context, "level"));
                                    return masteryInfo(context, player);
                                })))))
                .then(Commands.literal("add").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("mastery", StringArgumentType.string()).suggests(MASTERIES)
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0)).executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                    Optional<ResourceLocation> mastery = mastery(context, player);
                                    if (mastery.isEmpty()) return 0;
                                    MasteryService.add(player, mastery.get(), DoubleArgumentType.getDouble(context, "amount"));
                                    return masteryInfo(context, player);
                                })))));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> tree() {
        return Commands.literal("tree")
                .then(Commands.literal("info")
                        .executes(context -> treeInfo(context, context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player()).executes(context -> treeInfo(context, EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("points").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("branch", StringArgumentType.word()).suggests(BRANCHES)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 10_000)).executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                    Optional<ResourceLocation> mastery = mastery(context, player, StringArgumentType.getString(context, "branch"));
                                    if (mastery.isEmpty()) return 0;
                                    SkillTreeService.setPoints(player, mastery.get(), IntegerArgumentType.getInteger(context, "amount"));
                                    return treeInfo(context, player);
                                })))))
                .then(Commands.literal("buy").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("node", ResourceLocationArgument.id()).suggests(NODES).executes(context -> {
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            ResourceLocation node = ResourceLocationArgument.getId(context, "node");
                            SkillTreeRules.Result result = SkillTreeService.buy(player, node);
                            if (result != SkillTreeRules.Result.OK) {
                                context.getSource().sendFailure(Component.translatable(Ids.key("command", "tree.buy." + result.name().toLowerCase(Locale.ROOT)), node.toString()));
                                return 0;
                            }
                            return treeInfo(context, player);
                        }))))
                .then(Commands.literal("reset").then(Commands.argument("player", EntityArgument.player()).executes(context -> {
                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                    SkillTreeService.reset(player);
                    return treeInfo(context, player);
                })));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> rank() {
        return Commands.literal("rank")
                .then(Commands.literal("info")
                        .executes(context -> rankInfo(context, context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player()).executes(context -> rankInfo(context, EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("set").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("rank", StringArgumentType.word()).suggests(RANKS).executes(context -> {
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            String id = StringArgumentType.getString(context, "rank");
                            Optional<Rank> rank = Rank.byId(id);
                            if (rank.isEmpty()) {
                                context.getSource().sendFailure(Component.translatable(Ids.key("command", "rank.unknown"), id));
                                return 0;
                            }
                            RankService.set(player, rank.get());
                            return rankInfo(context, player);
                        }))));
    }

    private static Optional<ResourceLocation> mastery(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        return mastery(context, player, StringArgumentType.getString(context, "mastery"));
    }

    /** Short names for the mod's masteries; {@code grimoire} is the current element. Unknown or no grimoire fails. */
    private static Optional<ResourceLocation> mastery(CommandContext<CommandSourceStack> context, ServerPlayer player, String name) {
        Optional<ResourceLocation> id = switch (name) {
            case "body" -> Optional.of(Masteries.BODY);
            case "mana" -> Optional.of(MasteryService.resourceMastery(player));
            case "antimana" -> Optional.of(Masteries.ANTIMANA);
            case "grimoire" -> MasteryService.elementMastery(player);
            default -> Optional.ofNullable(ResourceLocation.tryParse(name))
                    .filter(rl -> GrimoireRegistries.ELEMENTS.containsKey(rl) || rl.equals(Masteries.ANTI_MAGIC));
        };
        if (id.isEmpty()) context.getSource().sendFailure(Component.translatable(Ids.key("command", "mastery.unknown"), name));
        return id;
    }

    private static int masteryInfo(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        Map<ResourceLocation, MasteryData.Entry> all = new TreeMap<>(MasteryService.data(player).masteries());
        all.putIfAbsent(Masteries.BODY, MasteryData.Entry.START);
        all.putIfAbsent(MasteryService.resourceMastery(player), MasteryData.Entry.START);
        MasteryService.elementMastery(player).ifPresent(element -> all.putIfAbsent(element, MasteryData.Entry.START));
        context.getSource().sendSuccess(() -> Component.translatable(Ids.key("command", "mastery.info"), player.getDisplayName()), false);
        all.forEach((id, entry) -> context.getSource().sendSuccess(() -> Component.translatable(Ids.key("command", "mastery.line"),
                MasteryService.name(id), entry.level(), String.format(Locale.ROOT, "%.1f", entry.progress()),
                String.format(Locale.ROOT, "%.0f", com.navesdev.grimoireknights.progression.rules.MasteryRules.required(entry.level(), MasteryService.curve())),
                MasteryService.ceiling(player, id)), false));
        return 1;
    }

    private static int treeInfo(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        SkillTreeData data = SkillTreeService.data(player);
        String points = data.points().entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey().getPath() + "=" + entry.getValue()).collect(Collectors.joining(", "));
        String nodes = data.nodes().entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey().getPath() + " " + entry.getValue()).collect(Collectors.joining(", "));
        context.getSource().sendSuccess(() -> Component.translatable(Ids.key("command", "tree.info"), player.getDisplayName(),
                points.isEmpty() ? "-" : points, nodes.isEmpty() ? "-" : nodes), false);
        return 1;
    }

    private static int rankInfo(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        context.getSource().sendSuccess(() -> Component.translatable(Ids.key("command", "rank.info"), player.getDisplayName(),
                RankService.name(RankService.rank(player)), RankService.ceiling(player)), false);
        GrimoireKnights.LOGGER.debug("Rank of {}: {}", player.getGameProfile().getName(), RankService.rank(player));
        return 1;
    }
}
