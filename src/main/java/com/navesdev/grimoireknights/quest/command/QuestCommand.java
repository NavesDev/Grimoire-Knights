package com.navesdev.grimoireknights.quest.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.quest.QuestService;
import com.navesdev.grimoireknights.quest.attachment.QuestData;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** {@code /gk quest info|give|clear <player> [quest]} */
public final class QuestCommand {
    private static final SuggestionProvider<CommandSourceStack> QUESTS =
            (context, builder) -> SharedSuggestionProvider.suggestResource(QuestService.ids(context.getSource().getServer()), builder);

    private QuestCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> branch() {
        return Commands.literal("quest")
                .then(Commands.literal("info").then(Commands.argument("player", EntityArgument.player()).executes(QuestCommand::info)))
                .then(Commands.literal("give").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("quest", ResourceLocationArgument.id()).suggests(QUESTS).executes(QuestCommand::give))))
                .then(Commands.literal("clear").then(Commands.argument("player", EntityArgument.player())
                        .executes(QuestCommand::clearAll)
                        .then(Commands.argument("quest", ResourceLocationArgument.id()).suggests(QUESTS).executes(QuestCommand::clear))));
    }

    private static int info(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        QuestData data = QuestService.data(player);
        String active = data.active().keySet().stream().map(ResourceLocation::toString).sorted().collect(Collectors.joining(", "));
        String done = data.completed().stream().map(ResourceLocation::toString).sorted().collect(Collectors.joining(", "));
        context.getSource().sendSuccess(() -> Component.translatable(Ids.key("command", "quest.info"),
                player.getDisplayName(), active.isEmpty() ? "-" : active, done.isEmpty() ? "-" : done), false);
        return 1;
    }

    private static int give(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ResourceLocation quest = ResourceLocationArgument.getId(context, "quest");
        QuestService.GiveResult result = QuestService.give(player, quest);
        if (result != QuestService.GiveResult.OK) {
            context.getSource().sendFailure(Component.translatable(Ids.key("command", "quest.give." + result.name().toLowerCase()), quest.toString()));
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.translatable(Ids.key("command", "quest.given"), quest.toString(), player.getDisplayName()), true);
        return 1;
    }

    private static int clear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ResourceLocation quest = ResourceLocationArgument.getId(context, "quest");
        QuestService.clear(player, quest);
        context.getSource().sendSuccess(() -> Component.translatable(Ids.key("command", "quest.cleared"), quest.toString(), player.getDisplayName()), true);
        return 1;
    }

    private static int clearAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        QuestService.clearAll(player);
        context.getSource().sendSuccess(() -> Component.translatable(Ids.key("command", "quest.cleared_all"), player.getDisplayName()), true);
        return 1;
    }
}
