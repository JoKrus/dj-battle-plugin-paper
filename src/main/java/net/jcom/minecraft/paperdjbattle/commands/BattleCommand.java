package net.jcom.minecraft.paperdjbattle.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.jcom.minecraft.paperdjbattle.PaperDjBattlePlugin;
import net.jcom.minecraft.paperdjbattle.config.BattleState;
import net.jcom.minecraft.paperdjbattle.config.BattleStateManager;
import net.jcom.minecraft.paperdjbattle.config.Defaults;
import net.jcom.minecraft.paperdjbattle.config.DefaultsManager;
import net.jcom.minecraft.paperdjbattle.database.entities.Team;
import net.jcom.minecraft.paperdjbattle.database.services.PlayerService;
import net.jcom.minecraft.paperdjbattle.database.services.TeamService;
import net.jcom.minecraft.paperdjbattle.event.BattleStartedEvent;
import net.jcom.minecraft.paperdjbattle.event.BattleStoppedEvent;
import net.jcom.minecraft.paperdjbattle.event.data.BattleData;
import net.jcom.minecraft.paperdjbattle.event.data.TeamConfig;
import net.jcom.minecraft.paperdjbattle.listeners.GracePeriodListener;
import net.jcom.minecraft.paperdjbattle.utils.CountdownTimer;
import net.jcom.minecraft.paperdjbattle.utils.DataUtils;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

public class BattleCommand {
    private static BukkitTask yBorderTask;

    public static LiteralCommandNode<CommandSourceStack> createCommand(final String commandName, TeamService teamService, PlayerService playerService) {
        return Commands.literal(commandName)
                .then(Commands.literal("start")
                        .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("battle-plugin.battle.start"))
                        .executes(commandContext -> {
                            var sender = commandContext.getSource().getSender();
                            return runStart(sender, "DJ-Classic " + UUID.randomUUID(), "classic", teamService, playerService);
                        })
                        .then(Commands.argument("battleName", StringArgumentType.string())
                                .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("battle-plugin.battle.start"))
                                .executes(commandContext -> {
                                    var sender = commandContext.getSource().getSender();
                                    String battleName = StringArgumentType.getString(commandContext, "battleName");
                                    return runStart(sender, battleName, "classic", teamService, playerService);
                                })
                                .then(Commands.argument("category", StringArgumentType.string())
                                        .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("battle-plugin.battle.start"))
                                        .executes(commandContext -> {
                                            var sender = commandContext.getSource().getSender();
                                            String category = StringArgumentType.getString(commandContext, "category");
                                            String battleName = StringArgumentType.getString(commandContext, "battleName");
                                            return runStart(sender, battleName, category, teamService, playerService);
                                        }))))
                .then(Commands.literal("stop")
                        .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("battle-plugin.battle.stop"))
                        .executes(commandContext -> {
                            var sender = commandContext.getSource().getSender();
                            return runStop(sender, false, teamService, playerService);
                        }).then(Commands.argument("wasCancelled", BoolArgumentType.bool())
                                .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("battle-plugin.battle.stop"))
                                .executes(context -> {
                                    var sender = context.getSource().getSender();
                                    var cancelled = BoolArgumentType.getBool(context, "wasCancelled");
                                    return runStop(sender, cancelled, teamService, playerService);
                                })))
                .then(Commands.literal("init")/*.requires(BattleNotGoingOn usw)*/
                        .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("battle-plugin.battle.init"))
                        .executes(commandContext -> {
                            var sender = commandContext.getSource().getSender();
                            return runInit(sender);
                        }))
                .then(Commands.literal("reload")
                        .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("battle-plugin.battle.reload"))
                        .executes(commandContext ->
                        {
                            var sender = commandContext.getSource().getSender();
                            return runReload(sender);
                        }))
                .then(Commands.literal("border-stop")
                        .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("battle-plugin.battle.border-stop"))
                        .executes(commandContext -> {
                            var sender = commandContext.getSource().getSender();
                            return runBorderStop(sender);
                        }))
                .build();
    }

    private static int runBorderStop(CommandSender sender) {
        if (yBorderTask != null && !yBorderTask.isCancelled()) {
            yBorderTask.cancel();
            yBorderTask = null;
            sender.sendMessage(text("Horizontal border disabled!"));
        } else {
            sender.sendMessage(text("No border present to disable!", RED));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int runReload(CommandSender sender) {
        PaperDjBattlePlugin.getPlugin().reloadConfig();
        sender.sendMessage("Config reloaded!");
        return Command.SINGLE_SUCCESS;
    }

    private static int runInit(CommandSender sender) {
        List<String> alwaysCmds = List.of(
                "gamerule sendCommandFeedback true",
                "defaultgamemode adventure",
                "gamerule doInsomnia false",
                "gamerule doTraderSpawning false",
                "gamerule logAdminCommands false",
                "gamerule commandBlockOutput false",
                "gamerule doWeatherCycle false",
                "gamerule doPatrolSpawning false",
                "gamerule disableRaids true"
        );

        List<String> noBattleCmds = List.of(
                "difficulty peaceful",
                "setworldspawn " + DefaultsManager.getValue(Defaults.LOBBY_LOCATION),
                "worldborder center " + getXZLoc(DefaultsManager.getValue(Defaults.LOBBY_LOCATION)),
                "worldborder set " + DefaultsManager.getValue(Defaults.WORLD_BORDER_LOBBY_WIDTH) + " 0"
        );

        ArrayList<String> cmds = new ArrayList<>(alwaysCmds);

        if (!BattleStateManager.get().isGoingOn()) {
            cmds.addAll(noBattleCmds);
        }

        for (var cmd : cmds) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        sender.sendMessage("Battle settings initialized");
        return Command.SINGLE_SUCCESS;
    }

    private static int runStop(CommandSender sender, boolean wasCancelled, TeamService teamService, PlayerService playerService) {
        if (BattleStateManager.get().getState() == BattleState.LOBBY) {
            sender.sendMessage(text("No battle present right now.", RED));
            return 0;
        }

        if (yBorderTask != null && !yBorderTask.isCancelled()) {
            yBorderTask.cancel();
            yBorderTask = null;
        }

        var previousState = BattleStateManager.get().getState(); //countdown or running
        BattleStateManager.get().setState(BattleState.LOBBY);

        Team winnerTeam = null;
        String winnerTeamName = null;
        BattleStoppedEvent stopEvent;
        if (previousState == BattleState.RUNNING && !wasCancelled) {
            winnerTeam = checkIfWon(teamService, playerService);
        }
        if (winnerTeam != null) {
            winnerTeamName = winnerTeam.getTeamname();
        }
        stopEvent = new BattleStoppedEvent(winnerTeamName, wasCancelled);


        List<String> cmds = List.of(
                "time set 0",
                "difficulty peaceful",
                "gamemode adventure @a",
                "effect clear @a",
                "clear @a",
                "worldborder center " + getXZLoc(DefaultsManager.getValue(Defaults.LOBBY_LOCATION)),
                "worldborder set " + DefaultsManager.getValue(Defaults.WORLD_BORDER_LOBBY_WIDTH) + " 0",
                "spreadplayers " + getXZLoc(DefaultsManager.getValue(Defaults.LOBBY_LOCATION)) + " 0 2 false @a"
        );

        if (previousState == BattleState.RUNNING) {
            for (var cmd : cmds) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }

        var component = text("Battle was stopped!");
        Bukkit.broadcast(component);
        if (!wasCancelled && winnerTeam != null) {
            var comp = text("", AQUA).append(text(winnerTeam.getTeamname(), AQUA, BOLD)).append(text(" has won the battle! Congratulations!"));
            Bukkit.broadcast(comp);
        }

        //clean up spectator


        stopEvent.callEvent();
        return Command.SINGLE_SUCCESS;
    }

    private static Team checkIfWon(TeamService teamService, PlayerService playerService) {
        List<Team> aliveTeams = teamService.getTeamsAlive();

        if (aliveTeams.size() == 1) {
            return aliveTeams.getFirst();
        } else {
            return null;
        }
    }

    private static int runStart(CommandSender sender, String battleName, String category, TeamService teamService, PlayerService playerService) {
        if (BattleStateManager.get().getState() != BattleState.LOBBY) {
            sender.sendMessage(text("Battle already going on.", RED));
            return 0;
        }

        BattleStateManager.get().setState(BattleState.COUNTDOWN);
        correctTeamData(teamService, playerService);

        var timer = new CountdownTimer(PaperDjBattlePlugin.getPlugin(),
                DefaultsManager.getValue(Defaults.BATTLE_START),
                () -> {
                },
                () -> {
                    onBattleStart(battleName, category, teamService, playerService);
                },
                countdownTimer -> {
                    if (BattleStateManager.get().getState() == BattleState.LOBBY) {
                        Bukkit.broadcast(text("Start has been cancelled!"));
                        countdownTimer.cancelTimer();
                        return;
                    }
                    //TODO maybe tp here etc

                    Bukkit.broadcast(text(countdownTimer.getSecondsLeft() + "..."));
                }
        );


        Bukkit.broadcast(text("Battle will start in"));
        timer.scheduleTimer();

        return Command.SINGLE_SUCCESS;
    }

    private static void onBattleStart(String battleName, String category, TeamService teamService, PlayerService playerService) {
        List<String> cmds = List.of(
                "time set 0",
                "weather clear",
                "effect clear @a",
                "clear @a",
                "difficulty normal",
                "give @a minecraft:bread 10",
                "experience set @a 0",
                "worldborder center " + getXZLoc(DefaultsManager.getValue(Defaults.BATTLE_LOCATION)),
                "worldborder set " + DefaultsManager.getValue(Defaults.WORLD_BORDER_INIT_WIDTH) + " 0",
                "worldborder set " + DefaultsManager.getValue(Defaults.WORLD_BORDER_END_WIDTH) + " " +
                        DefaultsManager.getValue(Defaults.BATTLE_DURATION),
                "spreadplayers " + getXZLoc(DefaultsManager.getValue(Defaults.BATTLE_LOCATION)) + " 1 "
                        + DefaultsManager.getValue(Defaults.BATTLE_LOCATION_SPREAD_RADIUS) + " under " +
                        (Integer.parseInt(getYLoc(DefaultsManager.getValue(Defaults.BATTLE_LOCATION))) + 12) + " true @a",
                "gamemode survival @a"
        );

        for (var cmd : cmds) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        for (var p : Bukkit.getOnlinePlayers().toArray(new Player[0])) {
            p.setSaturation(5);
            p.setFoodLevel(20);
            p.setHealth(20);
        }

        if (BattleStateManager.get().getState() == BattleState.LOBBY) {
            Bukkit.broadcast(text("Start has been cancelled!"));
            return;
        }
        var component = text().content("Battle ")
                .append(text(battleName, GOLD, BOLD))
                .append(text(" (%s) ".formatted(category), WHITE, ITALIC))
                .append(text("has started!"))
                .build();
        Bukkit.broadcast(component);

        BattleStateManager.get().setState(BattleState.RUNNING);

        //SpectatorManager.start();

        var gracePeriod = new GracePeriodListener();
        Bukkit.getServer().getPluginManager().registerEvents(gracePeriod, PaperDjBattlePlugin.getPlugin());

        // send event
        //db has been corrected here
        var allTeams = teamService.findAll();
        var players = playerService.findAll();
        var config = TeamConfig.getConfig(allTeams, players);
        var battleData = BattleData.getBattleData(battleName, category);
        Bukkit.getPluginManager().callEvent(new BattleStartedEvent(config, battleData));

        var graceTimer = new CountdownTimer(PaperDjBattlePlugin.getPlugin(),
                DefaultsManager.getValue(Defaults.GRACE_PERIOD),
                () -> {
                    Bukkit.broadcast(text(DefaultsManager.getValue(Defaults.GRACE_PERIOD) + " second grace period started!"));
                },
                () -> {
                    Bukkit.broadcast(text("Fighting begins!", Style.style(TextDecoration.BOLD)));
                    HandlerList.unregisterAll(gracePeriod);
                },
                countdownTimer -> {
                    if (!BattleStateManager.get().isGoingOn()) {
                        countdownTimer.cancelTimer();
                        return;
                    }

                    if (countdownTimer.getSecondsLeft() > 20) {
                        if (countdownTimer.getSecondsLeft() % 10 == 0) {
                            Bukkit.broadcast(text(countdownTimer.getSecondsLeft() + " seconds until grace period ends!"));
                        }
                    } else if (countdownTimer.getSecondsLeft() > 5) {
                        if (countdownTimer.getSecondsLeft() % 5 == 0) {
                            Bukkit.broadcast(text(countdownTimer.getSecondsLeft() + " seconds until grace period ends!"));
                        }
                    } else {
                        Bukkit.broadcast(text(countdownTimer.getSecondsLeft() + "..."));
                    }
                });
        graceTimer.scheduleTimer();

        //Todo retrigger when restarting server

        var renderDistance = DefaultsManager.<Integer>getValue(Defaults.HORIZONTAL_BORDER_RENDER_DISTANCE);
        var renderSize = DefaultsManager.<Integer>getValue(Defaults.HORIZONTAL_BORDER_RENDER_SIZE);
        var ySpawn = Double.parseDouble(getYLoc(DefaultsManager.getValue(Defaults.BATTLE_LOCATION)));
        var totalTime = DefaultsManager.<Integer>getValue(Defaults.BATTLE_DURATION);
        var delayTime = DefaultsManager.<Integer>getValue(Defaults.HORIZONTAL_BORDER_START);
        yBorderTask = Bukkit.getScheduler().runTaskTimer(PaperDjBattlePlugin.getPlugin(), () -> {
            var duration = BattleStateManager.get().incrementDuration();

            var yMin = DataUtils.lerpWithDelay(duration, delayTime, totalTime, -80, ySpawn - 10);
            var yMax = DataUtils.lerpWithDelay(duration, delayTime, totalTime, 350, ySpawn + 15);

            drawHorizontalBorderAndDamage(yMin, yMax, renderDistance, renderSize);
        }, 0, 20);

    }


    private static void drawHorizontalBorderAndDamage(double yLevelMin, double yLevelMax, int renderDistance, int renderSize) {
        var allPlayersCloseToOuts = Bukkit.getOnlinePlayers().stream().filter(player -> {
            var toLoc = player.getLocation();
            return (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.CREATIVE)
                    && (toLoc.y() <= yLevelMin + renderDistance || toLoc.y() >= yLevelMax - renderDistance);
        }).toList();

        for (var player : allPlayersCloseToOuts) {
            var toLoc = player.getLocation();
            double x = Math.floor(toLoc.getX()) + 0.5;
            double z = Math.floor(toLoc.getZ()) + 0.5;
            var yToCheckLow = toLoc.getY() + 0.1;
            var yToCheckTop = player.getEyeLocation().getY() - 0.1;

            for (int i = -renderSize; i <= renderSize; i++) {
                for (int j = -renderSize; j <= renderSize; j++) {
                    var particleTop = new Location(toLoc.getWorld(), x + i, yLevelMax, z + j);
                    var particleBot = new Location(toLoc.getWorld(), x + i, yLevelMin, z + j);
                    Particle.END_ROD.builder().location(particleTop).count(0).receivers(renderDistance + 15, false).spawn();
                    Particle.END_ROD.builder().location(particleBot).count(0).receivers(renderDistance + 15, false).spawn();
                }
            }

            if (player.getGameMode() == GameMode.SURVIVAL && (yToCheckLow <= yLevelMin || yToCheckTop >= yLevelMax)) {
                player.damage(2, DamageSource.builder(DamageType.OUT_OF_WORLD).build());
                player.sendActionBar(text("The horizontal border is close! Get to the center!", RED));
            } else {
                player.sendActionBar(text("The horizontal border is close! Get to the center!", YELLOW));
            }
        }
    }


    private static void correctTeamData(TeamService teamService, PlayerService playerService) {
        //remove offline players
        var onlineIds = Bukkit.getOnlinePlayers().stream().map(Entity::getUniqueId).toList();
        playerService.deleteNotMatching(onlineIds);
        teamService.removeEmptyTeams();

        //create teams for teamless players
        var teamlessPlayers = playerService.findTeamless();
        for (var player : teamlessPlayers) {
            try {
                teamService.createOrJoinTeam(player, player.getName());
            } catch (SQLException e) {
                //try again with teamname '<name> #2'
                try {
                    teamService.createOrJoinTeam(player, "%s #2".formatted(player.getName()));
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        teamService.setAllAlive();
    }

    private static String getXZLoc(String loc) {
        if (loc == null)
            return "0 0";

        var arr = loc.split("\\s+");
        return arr[0] + " " + arr[2];
    }

    private static String getYLoc(String loc) {
        if (loc == null)
            return "256";

        var arr = loc.split("\\s+");
        return arr[1];
    }
}
