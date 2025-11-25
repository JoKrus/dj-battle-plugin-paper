package net.jcom.minecraft.paperdjbattle.listeners;

import net.jcom.minecraft.paperdjbattle.PaperDjBattlePlugin;
import net.jcom.minecraft.paperdjbattle.config.BattleStateManager;
import net.jcom.minecraft.paperdjbattle.database.services.PlayerService;
import net.jcom.minecraft.paperdjbattle.database.services.TeamService;
import net.jcom.minecraft.paperdjbattle.utils.DataUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.nio.file.Paths;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class BattleHandler implements Listener {

    private final File battleData;
    private final TeamService teamService;
    private final PlayerService playerService;

    public BattleHandler(Plugin plugin, TeamService teamService, PlayerService playerService) {
        this.teamService = teamService;
        this.playerService = playerService;
        this.battleData = Paths.get(plugin.getDataFolder().getAbsolutePath(), "respawnData.yml").toFile();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        if (!BattleStateManager.get().isGoingOn()) {
            return;
        }

        var deathLocation = playerDeathEvent.getEntity().getLocation();
        DataUtils.setAndSave(battleData, playerDeathEvent.getEntity().getUniqueId() + ".location", deathLocation);

        if (playerDeathEvent.getEntity().getGameMode() == GameMode.SPECTATOR) {
            playerDeathEvent.deathMessage(null);
            //Spectator mode is bugged when switching to spectator during respawn so noclip and spec menu is not present
            //which is what we want so swapping here
            playerDeathEvent.getEntity().setGameMode(GameMode.SURVIVAL);
            return;
        }

        playerDeathEvent.deathMessage(text("A player has died.", AQUA));
        playerDeathEvent.getEntity().getWorld().strikeLightningEffect(deathLocation);
        for (var player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 1.f,
                    0.f);
        }

        var djPlayer = playerService.findByUuid(playerDeathEvent.getEntity().getUniqueId());
        var teamPlayers = teamService.getTeamMembersOfPlayer(djPlayer.getPlayerid());

        var anyAlive = false;
        for (var player : teamPlayers) {
            if (player.getPlayerid().equals(djPlayer.getPlayerid())) {
                continue;
            }
            var bukkitPlayer = Bukkit.getPlayer(player.getPlayerid());
            if (bukkitPlayer != null && bukkitPlayer.getGameMode() == GameMode.SURVIVAL && bukkitPlayer.getHealth() > 0) {
                anyAlive = true;
                break;
            }
        }

        if (!anyAlive) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(PaperDjBattlePlugin.getPlugin(), () -> {
                var comp = text().append(text("Team \"", AQUA))
                        .append(text(djPlayer.getTeam().getTeamname(), AQUA, BOLD))
                        .append(text("\" has been eliminated", AQUA));

                Bukkit.broadcast(comp.build());
            }, 10);
            djPlayer.getTeam().setEliminated(true);
            teamService.update(djPlayer.getTeam());
        }

        if (teamService.getTeamAmountAlive() == 1) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(PaperDjBattlePlugin.getPlugin(), () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "djbattle stop");
            }, 10);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent playerRespawnEvent) {
        if (BattleStateManager.get().isGoingOn()) {
            playerRespawnEvent.getPlayer().setGameMode(GameMode.SPECTATOR);

            //TODO check if teammate still alive, if yes to spectator mode fun
            FileConfiguration config = YamlConfiguration.loadConfiguration(battleData);
            var loc = config.getLocation(playerRespawnEvent.getPlayer().getUniqueId() + ".location");
            if (loc != null) {
                playerRespawnEvent.setRespawnLocation(loc);
            }
        }
        DataUtils.setAndSave(battleData, playerRespawnEvent.getPlayer().getUniqueId() + ".location", null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent playerQuitEvent) {
        if (playerQuitEvent.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            playerQuitEvent.getPlayer().setHealth(0);
        }
    }

    @EventHandler
    public void onGameMode(PlayerGameModeChangeEvent playerGameModeChangeEvent) {
        //TODO disable gamemode switching during battle
        /*
        if (BattleStateManager.get().isGoingOn()) {
            if (playerGameModeChangeEvent.getNewGameMode() == GameMode.CREATIVE) {
                playerGameModeChangeEvent.setCancelled(true);
                playerGameModeChangeEvent.cancelMessage(text("You're not allowed to change your gamemode!", RED));
            } else if (playerGameModeChangeEvent.getNewGameMode() == GameMode.SURVIVAL
                    && playerGameModeChangeEvent.getPlayer().getGameMode() == GameMode.SPECTATOR) {
                playerGameModeChangeEvent.setCancelled(true);
                playerGameModeChangeEvent.cancelMessage(text("You're not allowed to change your gamemode!", RED));
            }
        }
        */
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
        if (BattleStateManager.get().isGoingOn()) {
            playerJoinEvent.getPlayer().getInventory().clear();
            playerJoinEvent.getPlayer().setGameMode(GameMode.SPECTATOR);
            playerJoinEvent.getPlayer().sendMessage("Battle is already going on, so you are in spectator mode.");
            playerJoinEvent.getPlayer().setHealth(0);
        } else if (isOutsideBorder(playerJoinEvent.getPlayer())) {
            playerJoinEvent.getPlayer().setHealth(0);
            playerJoinEvent.getPlayer().setGameMode(GameMode.ADVENTURE);
        }
    }

    private static boolean isInsideBorder(Player player) {
        return !isOutsideBorder(player);
    }

    //https://www.spigotmc.org/threads/check-if-a-player-is-outside-border.176990/
    private static boolean isOutsideBorder(Player player) {
        WorldBorder border = player.getWorld().getWorldBorder();
        double radius = border.getSize() / 2;
        Location location = player.getLocation(), center = border.getCenter();

        return center.distanceSquared(location) >= (radius * radius);
    }
}
