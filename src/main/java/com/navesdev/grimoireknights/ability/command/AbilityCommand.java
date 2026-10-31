package com.navesdev.grimoireknights.ability.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.navesdev.grimoireknights.ability.AbilityService;
import com.navesdev.grimoireknights.core.Ids;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** {@code /gk mana info|current|fill ...} and {@code /gk cooldown reset <player>}. */
public final class AbilityCommand {
    private AbilityCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> mana() {
        return Commands.literal("mana")
                .then(Commands.literal("info")
                        .executes(context -> info(context, context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> info(context, EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("current").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0)).executes(context ->
                                setCurrent(context, DoubleArgumentType.getDouble(context, "value"))))))
                .then(Commands.literal("fill").then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> setCurrent(context, Double.MAX_VALUE))));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> cooldown() {
        return Commands.literal("cooldown").then(Commands.literal("reset").then(Commands.argument("player", EntityArgument.player())
                .executes(context -> {
                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                    AbilityService.resetCooldowns(player);
                    context.getSource().sendSuccess(() -> Component.translatable(Ids.key("command", "cooldown.reset"), player.getDisplayName()), true);
                    return 1;
                })));
    }

    private static int info(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        AbilityService.ensureMana(player);
        context.getSource().sendSuccess(() -> Component.translatable(Ids.key("command", "mana.info"), player.getDisplayName(),
                String.format(Locale.ROOT, "%.0f", AbilityService.maxMana(player)), String.format(Locale.ROOT, "%.1f", AbilityService.data(player).mana()),
                String.format(Locale.ROOT, "%.1f", AbilityService.regenPerSecond(player))), false);
        return 1;
    }

    private static int setCurrent(CommandContext<CommandSourceStack> context, double value) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        AbilityService.ensureMana(player);
        AbilityService.setMana(player, value);
        return info(context, player);
    }
}
