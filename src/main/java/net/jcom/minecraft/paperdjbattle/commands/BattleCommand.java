package net.jcom.minecraft.paperdjbattle.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.jcom.minecraft.paperdjbattle.config.BattleState;
import net.jcom.minecraft.paperdjbattle.config.Defaults;
import net.jcom.minecraft.paperdjbattle.config.DefaultsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

public class BattleCommand {
    public static LiteralCommandNode<CommandSourceStack> createCommand(final String commandName) {
        return Commands.literal(commandName)
                .then(Commands.literal("start")
                        .executes(commandContext -> {
                            return runStart("DJ-Classic " + UUID.randomUUID(), "classic");
                        })
                        .then(Commands.argument("battleName", StringArgumentType.string())
                                .executes(commandContext -> {
                                    String battleName = StringArgumentType.getString(commandContext, "battleName");

                                    return runStart(battleName, "classic");
                                })
                                .then(Commands.argument("category", StringArgumentType.string())
                                        .executes(commandContext -> {
                                            String category = StringArgumentType.getString(commandContext, "category");
                                            String battleName = StringArgumentType.getString(commandContext, "battleName");

                                            return runStart(battleName, category);
                                        }))))
                .then(Commands.literal("stop").executes(commandContext -> runStop()))
                .then(Commands.literal("init")/*.requires(BattleNotGoingOn usw)*/
                        .executes(commandContext -> {
                            var sender = commandContext.getSource().getSender();

                            return runInit(sender);
                        }))
                .then(Commands.literal("reload").executes(commandContext -> runReload()))
                .build();
    }

    private static int runReload() {
        var component = text("Battle has stopped!");

        Bukkit.broadcast(component);
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
        cmds.addAll(noBattleCmds);

        for (var cmd : cmds) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        sender.sendMessage("Battle settings initialized");
        return Command.SINGLE_SUCCESS;
    }

    private static int runStop() {
        var component = text("Battle has stopped!");
        Bukkit.broadcast(component);

        BattleState.get().setGoingOn(false);
        return Command.SINGLE_SUCCESS;
    }

    private static int runStart(String battleName, String category) {
        var component = text().content("Battle ")
                .append(text(battleName, GOLD, BOLD))
                .append(text(" (%s) ".formatted(category), WHITE, ITALIC))
                .append(text("has started!"))
                .build();
        Bukkit.broadcast(component);

        BattleState.get().setGoingOn(true);

        return Command.SINGLE_SUCCESS;
    }

    private static String getXZLoc(String loc) {
        if (loc == null)
            return "0 0";

        var arr = loc.split("\\s+");
        return arr[0] + " " + arr[2];
    }
}
