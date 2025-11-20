package net.jcom.minecraft.paperdjbattle;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.jcom.minecraft.paperdjbattle.commands.BattleCommand;
import net.jcom.minecraft.paperdjbattle.commands.TeamCommand;
import net.jcom.minecraft.paperdjbattle.config.BattleState;
import net.jcom.minecraft.paperdjbattle.config.Defaults;
import net.jcom.minecraft.paperdjbattle.config.DefaultsManager;
import net.jcom.minecraft.paperdjbattle.database.impl.SqliteDatabase;
import net.jcom.minecraft.paperdjbattle.database.services.PlayerService;
import net.jcom.minecraft.paperdjbattle.database.services.TeamService;
import net.jcom.minecraft.paperdjbattle.listeners.JoinListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public final class PaperDjBattlePlugin extends JavaPlugin implements Listener {

    private SqliteDatabase database;
    private PlayerService playerService;
    private TeamService teamService;

    @Override
    public void onEnable() {
        // Plugin startup logic

        this.getLogger().info("[PaperDjBattle] Plugin has been enabled!");

        BattleState.get().init(this);

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


        //this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getPluginManager().registerEvents(new JoinListener(playerService), this);

        DefaultsManager.init(this, Defaults.class);


        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(BattleCommand.createCommand("djbattle"), "Manages the battle (Start, Stop, Init etc.)", List.of());
            commands.registrar().register(TeamCommand.createCommand("djteam", teamService, playerService), "Used for team creation, joining a team and leaving a team", List.of());
            commands.registrar().register(TeamCommand.createCommand("djspectate", teamService, playerService), "Used to navigate spectating during a battle", List.of("djspec"));
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

    }
}
