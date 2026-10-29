package com.navesdev.grimoireknights.grimoire.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.grimoire.CeremonyService;
import com.navesdev.grimoireknights.grimoire.GrimoireRegistries;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import com.navesdev.grimoireknights.grimoire.attachment.GrimoireData;
import com.navesdev.grimoireknights.grimoire.rules.ElementLottery;
import com.navesdev.grimoireknights.grimoire.network.PlayerAnimationPayload;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.network.PacketDistributor;

/** Test commands of the grimoire feature: grimoire, ceremony, calling and anim branches. */
public final class GrimoireCommand {
    private static final SuggestionProvider<CommandSourceStack> ELEMENTS =
            (context, builder) -> SharedSuggestionProvider.suggestResource(GrimoireRegistries.ELEMENTS.keySet(), builder);

    private GrimoireCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> grimoire() {
        return Commands.literal("grimoire")
                .then(Commands.literal("info")
                        .executes(context -> info(context, context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> info(context, EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("set").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("element", ResourceLocationArgument.id()).suggests(ELEMENTS)
                                .executes(context -> set(context, GrimoireService.CEREMONY_LEAVES))
                                .then(Commands.argument("leaves", IntegerArgumentType.integer(3, 4))
                                        .executes(context -> set(context, IntegerArgumentType.getInteger(context, "leaves")))))))
                .then(Commands.literal("clear").then(Commands.argument("player", EntityArgument.player()).executes(GrimoireCommand::clear)))
                .then(Commands.literal("lottery").then(Commands.argument("times", IntegerArgumentType.integer(1, 1_000_000))
                        .executes(GrimoireCommand::lottery)));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> ceremony() {
        return Commands.literal("ceremony").then(Commands.literal("start").then(Commands.argument("player", EntityArgument.player())
                .executes(GrimoireCommand::ceremonyStart)));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> calling() {
        return Commands.literal("calling")
                .then(Commands.literal("info").then(Commands.argument("player", EntityArgument.player()).executes(GrimoireCommand::callingInfo)))
                .then(Commands.literal("set").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                .executes(context -> callingSet(context, DoubleArgumentType.getDouble(context, "value"))))))
                .then(Commands.literal("fill").then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> callingSet(context, GrimoireService.callingMax()))));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> anim() {
        return Commands.literal("anim").then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("animation", ResourceLocationArgument.id()).executes(GrimoireCommand::anim)));
    }

    private static int info(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        GrimoireData data = GrimoireService.data(player);
        Component element = data.element().map(GrimoireService::elementName).orElse(Component.translatable(Ids.key("command", "grimoire.none")));
        context.getSource().sendSuccess(() -> Component.translatable(Ids.key("command", "grimoire.info"),
                player.getDisplayName(), element, data.leaves(), data.ceremonyDone()), false);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> context, int leaves) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ResourceLocation element = ResourceLocationArgument.getId(context, "element");
        if (!GrimoireRegistries.ELEMENTS.containsKey(element)) {
            context.getSource().sendFailure(Component.translatable(Ids.key("command", "grimoire.unknown_element"), element.toString()));
            return 0;
        }
        GrimoireService.give(player, element, leaves);
        context.getSource().sendSuccess(() -> Component.translatable(Ids.key("command", "grimoire.set"),
                player.getDisplayName(), GrimoireService.elementName(element), leaves), true);
        return 1;
    }

    private static int clear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        GrimoireService.clear(player);
        context.getSource().sendSuccess(() -> Component.translatable(Ids.key("command", "grimoire.cleared"), player.getDisplayName()), true);
        return 1;
    }

    private static int lottery(CommandContext<CommandSourceStack> context) {
        int times = IntegerArgumentType.getInteger(context, "times");
        RandomSource random = context.getSource().getLevel().getRandom();
        Map<Optional<ResourceLocation>, Integer> weights = GrimoireService.lotteryWeights();
        Map<Optional<ResourceLocation>, Integer> counts = new LinkedHashMap<>();
        weights.keySet().forEach(key -> counts.put(key, 0));
        for (int i = 0; i < times; i++) {
            counts.merge(ElementLottery.draw(weights, random::nextInt), 1, Integer::sum);
        }
        MutableComponent result = Component.empty();
        counts.forEach((key, count) -> result
                .append(key.map(GrimoireService::elementName).orElse(Component.translatable(Ids.key("command", "grimoire.no_magic"))))
                .append(String.format(Locale.ROOT, " %d (%.1f%%)  ", count, 100.0 * count / times)));
        context.getSource().sendSuccess(() -> Component.translatable(Ids.key("command", "grimoire.lottery"), times, result), false);
        return 1;
    }

    private static int ceremonyStart(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        CeremonyService.Result result = CeremonyService.start(player, null);
        context.getSource().sendSuccess(() -> Component.translatable(Ids.key("command", "ceremony." + result.name().toLowerCase()),
                player.getDisplayName()), true);
        return result == CeremonyService.Result.ALREADY_DONE ? 0 : 1;
    }

    private static int callingInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        context.getSource().sendSuccess(() -> Component.translatable(Ids.key("command", "calling.info"), player.getDisplayName(),
                String.format(Locale.ROOT, "%.1f", GrimoireService.data(player).calling()), String.format(Locale.ROOT, "%.0f", GrimoireService.callingMax())), false);
        return 1;
    }

    private static int callingSet(CommandContext<CommandSourceStack> context, double value) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        GrimoireService.setCalling(player, value);
        return callingInfo(context);
    }

    private static int anim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ResourceLocation animation = ResourceLocationArgument.getId(context, "animation");
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new PlayerAnimationPayload(player.getId(), animation));
        context.getSource().sendSuccess(() -> Component.translatable(Ids.key("command", "anim.played"), animation.toString(), player.getDisplayName()), false);
        return 1;
    }
}
