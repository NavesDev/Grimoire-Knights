package com.navesdev.grimoireknights.world.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.world.WorldStructures;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/** {@code /gk tower place [pos]}: places the Grimoire Tower, door to the south, corner at {@code pos}. */
public final class TowerCommand {
    private TowerCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> branch() {
        return Commands.literal("tower").then(Commands.literal("place")
                .executes(context -> place(context, BlockPos.containing(context.getSource().getPosition())))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> place(context, BlockPosArgument.getLoadedBlockPos(context, "pos")))));
    }

    private static int place(CommandContext<CommandSourceStack> context, BlockPos pos) {
        ServerLevel level = context.getSource().getLevel();
        Optional<StructureTemplate> template = level.getStructureManager().get(WorldStructures.TOWER_TEMPLATE);
        if (template.isEmpty()) {
            context.getSource().sendFailure(Component.translatable(Ids.key("command", "tower.missing")));
            return 0;
        }
        template.get().placeInWorld(level, pos, pos, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_CLIENTS);
        context.getSource().sendSuccess(() -> Component.translatable(Ids.key("command", "tower.placed"),
                pos.getX(), pos.getY(), pos.getZ()), true);
        return 1;
    }
}
