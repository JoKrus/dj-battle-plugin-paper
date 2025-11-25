package net.jcom.minecraft.paperdjbattle.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.jcom.minecraft.paperdjbattle.config.BattleStateManager;
import net.jcom.minecraft.paperdjbattle.database.services.PlayerService;
import net.jcom.minecraft.paperdjbattle.database.services.TeamService;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import static net.kyori.adventure.text.Component.text;

public class SpectateCommand {
    public static LiteralCommandNode<CommandSourceStack> createCommand(final String commandName, TeamService teamService, PlayerService playerService) {
        return Commands.literal(commandName)
                .then(Commands.argument("target", ArgumentTypes.player())
                        .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("battle-plugin.spectate"))
                        .executes(context -> {
                            return runSpectateTarget(context, teamService, playerService);
                        }))
                .build();
    }

    private static int runSpectateTarget(CommandContext<CommandSourceStack> context, TeamService teamService, PlayerService playerService) throws CommandSyntaxException {
        var sender = context.getSource().getSender();
        var target = context.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).get(0);

        if (!(sender instanceof Player p)) {
            var component = text("Only players can use this command.", NamedTextColor.RED);
            sender.sendMessage(component);
            return 0;
        }

        if (!BattleStateManager.get().isGoingOn()) {
            var component = text("Not available when no battle is going on.", NamedTextColor.RED);
            sender.sendMessage(component);
            return 0;
        }

        if (p.getGameMode() != GameMode.SPECTATOR) {
            var component = text("Only usable in SpectatorMode.", NamedTextColor.RED);
            sender.sendMessage(component);
            return 0;
        }

        if (target.getGameMode() != GameMode.SURVIVAL) {
            var component = text("Your target \"" + target.getName() + "\" has to be in survival mode. " +
                    "Currently in " + target.getGameMode() + ".", NamedTextColor.RED);
            sender.sendMessage(component);
            return 0;
        }

        var djPl = playerService.findByUuid(p.getUniqueId());
        var djTarget = playerService.findByUuid(target.getUniqueId());
        var team = djPl.getTeam();
        var teamMembers = djPl.getTeamMembersAlive();

        if (!teamMembers.isEmpty() && djTarget.getTeam().getId() != team.getId()) {
            var component = text("You can only spectate your team until your team is out of the battle.", NamedTextColor.RED);
            sender.sendMessage(component);
            return 0;
        }

        djPl.setSpectateTarget(djTarget);
        playerService.update(djPl);

        sender.sendMessage("You are now spectating " + target.getName());

        return Command.SINGLE_SUCCESS;
    }
}
