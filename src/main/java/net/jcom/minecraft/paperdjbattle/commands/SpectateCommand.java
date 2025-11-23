package net.jcom.minecraft.paperdjbattle.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.jcom.minecraft.paperdjbattle.database.services.PlayerService;
import net.jcom.minecraft.paperdjbattle.database.services.TeamService;

public class SpectateCommand {
    public static LiteralCommandNode<CommandSourceStack> createCommand(final String commandName, TeamService teamService, PlayerService playerService) {
        return Commands.literal(commandName)
                .then(Commands.argument("target", ArgumentTypes.player())
                        .executes(context -> {
                            return runSpectateTarget(context, teamService, playerService);
                        }))
                .build();
    }

    private static int runSpectateTarget(CommandContext<CommandSourceStack> context, TeamService teamService, PlayerService playerService) {


        return Command.SINGLE_SUCCESS;
    }
}
