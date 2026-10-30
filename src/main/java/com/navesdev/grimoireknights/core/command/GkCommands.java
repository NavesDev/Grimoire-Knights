package com.navesdev.grimoireknights.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.navesdev.grimoireknights.GrimoireKnights;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/** Test commands live under {@code /grimoireknights} (alias {@code /gk}), permission level 2. Each feature adds its branch. */
public final class GkCommands {
    public static final String ROOT = GrimoireKnights.MODID;
    public static final String ALIAS = "gk";
    public static final int PERMISSION = 2;

    private GkCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> branch) {
        for (String root : new String[] {ROOT, ALIAS}) {
            dispatcher.register(Commands.literal(root).requires(source -> source.hasPermission(PERMISSION)).then(branch));
        }
    }
}
