package net.jcom.minecraft.paperdjbattle.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.jcom.minecraft.paperdjbattle.database.services.PlayerService;
import net.jcom.minecraft.paperdjbattle.database.services.TeamService;

public class SpectateCommand {
    public static LiteralCommandNode<CommandSourceStack> createCommand(final String commandName, TeamService teamService, PlayerService playerService) {
        return Commands.literal(commandName).build();
    }
}
