package net.jcom.minecraft.paperdjbattle.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import it.unimi.dsi.fastutil.Pair;
import net.jcom.minecraft.paperdjbattle.config.BattleState;
import net.jcom.minecraft.paperdjbattle.database.entities.DjPlayer;
import net.jcom.minecraft.paperdjbattle.database.entities.Team;
import net.jcom.minecraft.paperdjbattle.database.services.PlayerService;
import net.jcom.minecraft.paperdjbattle.database.services.TeamService;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.UNDERLINED;

public class TeamCommand {

    public static LiteralCommandNode<CommandSourceStack> createCommand(final String commandName, TeamService teamService, PlayerService playerService) {
        return Commands.literal(commandName)
                .then(Commands.literal("join")
                        .then(Commands.argument("teamName", StringArgumentType.greedyString())
                                //suggest existing teamnames .suggests()
                                .suggests((context, builder) -> getTeamSuggestions(teamService, context, builder))
                                .executes(commandContext -> runJoin(teamService, playerService, commandContext))))
                .then(Commands.literal("leave")
                        .executes(commandContext -> runLeave(teamService, playerService, commandContext)))
                .then(Commands.literal("list")
                        .executes(commandContext -> runList(teamService, playerService, commandContext)))
                .then(Commands.literal("test")
                        .requires(ctx -> ctx.getSender().hasPermission("battle-plugin.team.test"))
                        .executes(context -> runTest(teamService, playerService, context)))
                .then(Commands.literal("remove")
                        .then(Commands.argument("teamName", StringArgumentType.greedyString())
                                .requires(ctx -> ctx.getSender().hasPermission("battle-plugin.team.remove"))
                                .suggests((context, builder) -> getTeamSuggestions(teamService, context, builder))
                                .executes(context -> runRemove(teamService, playerService, context))))
                .build();
    }

    private static LocalDateTime lastSuggestRead = LocalDateTime.now();
    private static volatile List<String> teamNames = new ArrayList<>();

    private static CompletableFuture<Suggestions> getTeamSuggestions(TeamService teamService, CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        if (LocalDateTime.now().minusSeconds(2).isAfter(lastSuggestRead)) {
            var names = teamService.findAllSorted();
            teamNames = names.stream().map(Team::getTeamname).toList();
            lastSuggestRead = LocalDateTime.now();
        }

        for (var name : teamNames) {
            builder.suggest(name);
        }

        return builder.buildFuture();
    }

    private static int runRemove(TeamService teamService, PlayerService playerService, CommandContext<CommandSourceStack> context) {
        var sender = context.getSource().getSender();
        var teamName = context.getArgument("teamName", String.class);

        if (BattleState.get().isGoingOn()) {
            var component = text("You can't remove a team during a battle.", NamedTextColor.RED);
            sender.sendMessage(component);
            return 0;
        }

        var teamToDel = teamService.findByName(teamName);

        if (teamToDel == null) {
            var component = text("This team does not exist.", NamedTextColor.RED);
            sender.sendMessage(component);
            return 0;
        }

        var membersToDel = teamToDel.getTeamMembers().stream().toList();

        teamService.removeTeam(teamToDel);

        for (var playersInTeam : membersToDel) {
            var player = Bukkit.getPlayer(playersInTeam.getPlayerid());
            if (player != null) {
                player.sendMessage("Your team was removed by an admin!");
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int runTest(TeamService teamService, PlayerService playerService, CommandContext<CommandSourceStack> context) {
        var onlinePlayers = Bukkit.getOnlinePlayers().stream()
                .map(player -> Pair.of(player.getUniqueId(), player)).toList();
        var noTeamDjPlayers = playerService.findTeamless();
        var noTeamDjUuids = noTeamDjPlayers.stream().map(DjPlayer::getPlayerid).toList();

        var onlineAndNoTeam = onlinePlayers.stream()
                .filter(uuidPair -> noTeamDjUuids.contains(uuidPair.left())).toList();

        for (var player : onlineAndNoTeam) {
            player.right().sendMessage("You need to join a team via /djteam join <Teamname> to participate!");
        }


        var comp = text();
        if (!onlineAndNoTeam.isEmpty()) {
            comp.append(text("The following players are not yet in a team:\n\n", WHITE, UNDERLINED));

            var message = new StringBuilder();
            for (var player : onlineAndNoTeam) {
                message.append(player.right().getName()).append(", ");
            }
            message.delete(message.length() - 2, message.length());
            comp.append(text(message.toString()));
        } else {
            comp.append(text("Every player is in a team!"));
        }

        var onlineOps =
                Bukkit.getOperators().stream().filter(OfflinePlayer::isOnline).map(OfflinePlayer::getPlayer).toList();
        for (var op : onlineOps) {
            if (op != null) {
                op.sendMessage(comp);
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int runList(TeamService teamService, PlayerService playerService, CommandContext<CommandSourceStack> commandContext) {
        var sender = commandContext.getSource().getSender();

        var list = teamService.findAll();
        if (list.isEmpty()) {
            var component = text("No teams are currently registered!", NamedTextColor.RED);
            sender.sendMessage(component);
            return 0;
        }

        var comp = text().append(text("List of all teams", WHITE, UNDERLINED, BOLD)).append(text(" (%d)".formatted(list.size()), WHITE, UNDERLINED, BOLD)).append(text("\n\n"));

        for (var team : list) {
            comp.append(text(team.getTeamname(), WHITE, BOLD)).append(text(":"));

            StringBuilder sb = new StringBuilder();
            for (var player : team.getTeamMembers()) {
                sb.append(" ").append(player.getName()).append(",");
            }
            sb.replace(sb.length() - 1, sb.length(), "\n");
            comp.append(text(sb.toString(), WHITE));
        }
        sender.sendMessage(comp);
        return Command.SINGLE_SUCCESS;
    }

    private static int runLeave(TeamService teamService, PlayerService playerService, CommandContext<CommandSourceStack> commandContext) {
        var sender = commandContext.getSource().getSender();

        if (!(sender instanceof Player p)) {
            var component = text("Only players can use this command.", NamedTextColor.RED);
            sender.sendMessage(component);
            return 0;
        }

        if (BattleState.get().isGoingOn()) {
            var component = text("You can't leave a team during a battle.", NamedTextColor.RED);
            sender.sendMessage(component);
            return 0;
        }

        var djPlayer = playerService.findByUuid(p.getUniqueId());
        if (djPlayer == null) {
            var component = text("Error, you are not in the DB for some reason!", NamedTextColor.RED);
            p.sendMessage(component);
            return 0;
        }

        var oldTeam = djPlayer.getTeam();
        if (oldTeam == null) {
            var component = text("You are not in a team!", NamedTextColor.RED);
            p.sendMessage(component);
            return 0;
        }
        try {
            teamService.leaveOrDeleteTeam(djPlayer);

            var component = text("You left \"" + oldTeam.getTeamname() + "\" successfully.");
            p.sendMessage(component);

            var members = teamService.getTeamMembers(oldTeam.getId());
            if (members != null && !members.isEmpty()) {
                for (var teamMembers : members) {
                    if (teamMembers.getPlayerid().equals(djPlayer.getPlayerid())) {
                        continue;
                    }
                    var joinedComponent = text(p.getName()).append(text(" just left your team!"));
                    var teamPlayer = Bukkit.getPlayer(teamMembers.getPlayerid());
                    if (teamPlayer != null) {
                        teamPlayer.sendMessage(joinedComponent);
                    }
                }
            }


        } catch (SQLException e) {
            var component = text("An error occurred while leaving the team. Try again later.", NamedTextColor.RED);
            p.sendMessage(component);
            e.printStackTrace();
            return 0;
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int runJoin(TeamService teamService, PlayerService playerService, CommandContext<CommandSourceStack> commandContext) {
        //if in a team or team full, throw error

        var sender = commandContext.getSource().getSender();
        var name = commandContext.getArgument("teamName", String.class);

        if (!(sender instanceof Player p)) {
            var component = text("Only players can use this command.", NamedTextColor.RED);
            sender.sendMessage(component);
            return 0;
        }

        if (BattleState.get().isGoingOn()) {
            var component = text("You can't join a team during a battle.", NamedTextColor.RED);
            sender.sendMessage(component);
            return 0;
        }

        var djPlayer = playerService.findByUuid(p.getUniqueId());
        if (djPlayer == null) {
            var component = text("Error, you are not in the DB for some reason!", NamedTextColor.RED);
            p.sendMessage(component);
            return 0;
        }

        if (djPlayer.getTeam() != null) {
            var component = text("You are already in a team!", NamedTextColor.RED);
            p.sendMessage(component);
            return 0;
        }

        try {
            Team team = teamService.createOrJoinTeam(djPlayer, name);
            if (team == null) {
                var component = text("Joining the team failed. You are either in a team already or the " + "team you tried to join was full.", NamedTextColor.RED);
                p.sendMessage(component);
            } else {
                var component = text("You joined \"" + team.getTeamname() + "\" successfully.");
                p.sendMessage(component);

                team = teamService.findById(team.getId());

                for (var teamMembers : team.getTeamMembers()) {
                    if (teamMembers.getPlayerid().equals(djPlayer.getPlayerid())) {
                        continue;
                    }
                    var joinedComponent = text(p.getName()).append(text(" just joined your team!"));
                    var teamPlayer = Bukkit.getPlayer(teamMembers.getPlayerid());
                    if (teamPlayer != null) {
                        teamPlayer.sendMessage(joinedComponent);
                    }
                }
            }
        } catch (SQLException e) {
            var component = text("An error occurred while creating the team. Try again later.", NamedTextColor.RED);
            p.sendMessage(component);
            e.printStackTrace();
            return 0;
        }
        return Command.SINGLE_SUCCESS;
    }
}
