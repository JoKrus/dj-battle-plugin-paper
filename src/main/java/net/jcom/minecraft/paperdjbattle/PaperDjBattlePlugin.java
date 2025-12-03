package net.jcom.minecraft.paperdjbattle;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.jcom.minecraft.paperdjbattle.commands.BattleCommand;
import net.jcom.minecraft.paperdjbattle.commands.SpectateCommand;
import net.jcom.minecraft.paperdjbattle.commands.TeamCommand;
import net.jcom.minecraft.paperdjbattle.config.BattleStateManager;
import net.jcom.minecraft.paperdjbattle.config.Defaults;
import net.jcom.minecraft.paperdjbattle.config.DefaultsManager;
import net.jcom.minecraft.paperdjbattle.database.impl.SqliteDatabase;
import net.jcom.minecraft.paperdjbattle.database.services.PlayerService;
import net.jcom.minecraft.paperdjbattle.database.services.TeamService;
import net.jcom.minecraft.paperdjbattle.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public final class PaperDjBattlePlugin extends JavaPlugin implements Listener {
    private static PaperDjBattlePlugin plugin;

    private SqliteDatabase database;
    private PlayerService playerService;
    private TeamService teamService;

    @Override
    public void onEnable() {
        //TODO reset border movements on init start if battle runs
        //TODO add notification/ad for /djspec
        //TODO fix spectator getspectatingteammates query logic
        //TODO reenable gamemode switcher interrupt when done
        //TODO create and enhance readme (server.properties spawn protection 0, allow nether etc)
        //TODO add all tabtps commands as allowed
        //TODO make allowed commands configurable


        // Plugin startup logic
        plugin = this;
        this.getLogger().info("[PaperDjBattle] Plugin has been enabled!");

        DefaultsManager.init(this, Defaults.class);
        BattleStateManager.get().init(this);

        try {
            if (!getDataFolder().exists()) {
                //noinspection ResultOfMethodCallIgnored
                getDataFolder().mkdirs();
            }

            var dbPath = getDataFolder().getAbsolutePath() + File.separator + "database.db";
            database = new SqliteDatabase(dbPath);

            playerService = new PlayerService(database.getPlayerDao());
            teamService = new TeamService(database.getTeamDao(), database.getPlayerDao());
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().warning("[PaperDjBattle] Could not create database! Shutting down plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(BattleCommand.createCommand("djbattle", teamService, playerService), "Manages the battle (Start, Stop, Init etc.)", List.of());
            commands.registrar().register(TeamCommand.createCommand("djteam", teamService, playerService), "Used for team creation, joining a team and leaving a team", List.of());
            commands.registrar().register(SpectateCommand.createCommand("djspectate", teamService, playerService), "Used to navigate spectating during a battle", List.of("djspec"));
        });

        this.getServer().getPluginManager().registerEvents(new LobbyListener(this), this);
        this.getServer().getPluginManager().registerEvents(new BattleHandler(this, teamService, playerService), this);
        this.getServer().getPluginManager().registerEvents(new PlayerCommandSendListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PreventBedListener(this), this);
        this.getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        this.getServer().getPluginManager().registerEvents(new DbJoinListener(playerService), this);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            var console = Bukkit.getServer().getConsoleSender();
            String command = "djbattle init";
            Bukkit.dispatchCommand(console, command);
        }, 1);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        this.getLogger().info("[PaperDjBattle] Plugin has been disabled!");
    }

    public static PaperDjBattlePlugin getPlugin() {
        return plugin;
    }

    public static String getWorldName() {
        Properties serverProperties = new Properties();
        String mainWorldName;
        try {
            serverProperties.load(Files.newInputStream(Paths.get("server.properties")));
            mainWorldName = serverProperties.getProperty("level-name");
            if (mainWorldName == null) {
                getPlugin().getLogger().severe("server.properties file is missing or broken. Continuing may result in undefined behaviour.");
                mainWorldName = "world";
            }
        } catch (IOException e) {
            getPlugin().getLogger().severe("server.properties file is missing or broken. Continuing may result in undefined behaviour.");
            throw new RuntimeException(e);
        }
        return mainWorldName;
    }
}
