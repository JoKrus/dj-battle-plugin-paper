package net.jcom.minecraft.paperdjbattle.listeners;

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import net.jcom.minecraft.paperdjbattle.PaperDjBattlePlugin;
import net.jcom.minecraft.paperdjbattle.config.BattleStateManager;
import net.jcom.minecraft.paperdjbattle.database.entities.DjPlayer;
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
import java.time.LocalDateTime;
import java.util.HashMap;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
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

        if (playerDeathEvent.getPlayer().getGameMode() == GameMode.SPECTATOR) {
            playerDeathEvent.deathMessage(null);
        } else {
            playerDeathEvent.deathMessage(text("A player has died.", AQUA));
            playerDeathEvent.getEntity().getWorld().strikeLightningEffect(deathLocation);
            for (var player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 1.f,
                        0.f);
            }
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

        if (!anyAlive && playerDeathEvent.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(PaperDjBattlePlugin.getPlugin(), () -> {
                var comp = text().append(text("Team \"", AQUA))
                        .append(text(djPlayer.getTeam().getTeamname(), AQUA, BOLD))
                        .append(text("\" has been eliminated", AQUA));

                Bukkit.broadcast(comp.build());
            }, 10);
            djPlayer.getTeam().setEliminated(true);
            teamService.update(djPlayer.getTeam());
        } else {
            //move all spectating teammemvers to other teammate
            //TODO check if works
            //TODO there are some problems
            var teamMateSpecs = playerService.findAllSpectatingTeammates(djPlayer.getPlayerid());
            //check if teamMateSpecs is correctly filled

            var alive = djPlayer.getTeamMembersAlive();
            var aliveFirst = alive.getFirst();
            for (var teamMate : teamMateSpecs) {
                teamMate.setSpectateTarget(aliveFirst);
                playerService.update(teamMate);
            }
        }

        var specs = playerService.findAllSpectators(djPlayer.getPlayerid());
        for (var spec : specs) {
            spec.setSpectateTarget(null);
            playerService.update(spec);
        }


        if (teamService.getTeamAmountAlive() == 1) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(PaperDjBattlePlugin.getPlugin(), () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "djbattle stop");
            }, 10);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent playerRespawnEvent) {
        var pl = playerRespawnEvent.getPlayer();
        if (BattleStateManager.get().isGoingOn()) {
            playerRespawnEvent.getPlayer().setGameMode(GameMode.SPECTATOR);

            var djPl = playerService.findByUuid(pl.getUniqueId());
            var teamMembers = djPl.getTeamMembersAlive();

            if (!teamMembers.isEmpty()) {
                var djTarget = teamMembers.getFirst();
                var target = Bukkit.getPlayer(djTarget.getPlayerid());
                if (target != null) {
                    playerRespawnEvent.setRespawnLocation(target.getLocation());
                    Bukkit.getScheduler().scheduleSyncDelayedTask(PaperDjBattlePlugin.getPlugin(), () -> {
                        tpAndSpectate(djPl, djTarget);
                    }, 5);
                }
            } else {
                FileConfiguration config = YamlConfiguration.loadConfiguration(battleData);
                var loc = config.getLocation(pl.getUniqueId() + ".location");
                if (loc != null) {
                    playerRespawnEvent.setRespawnLocation(loc);
                }
            }
        }
        DataUtils.setAndSave(battleData, pl.getUniqueId() + ".location", null);
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
            if (playerJoinEvent.getPlayer().getGameMode() == GameMode.SPECTATOR) {
                playerJoinEvent.getPlayer().setHealth(0);
            } else {
                playerJoinEvent.getPlayer().getInventory().clear();
                playerJoinEvent.getPlayer().setGameMode(GameMode.SPECTATOR);
                playerJoinEvent.getPlayer().sendMessage("Battle is already going on, so you are in spectator mode.");
                playerJoinEvent.getPlayer().setHealth(0);
            }
        } else if (isOutsideBorder(playerJoinEvent.getPlayer())) {
            playerJoinEvent.getPlayer().setHealth(0);
            playerJoinEvent.getPlayer().setGameMode(GameMode.ADVENTURE);
        }
    }


    private static final HashMap<Player, LocalDateTime> lastMessageSend = new HashMap<>();

    @EventHandler
    public void onSpectatorLeave(PlayerStopSpectatingEntityEvent playerStopSpectatingEntityEvent) {
        var pl = playerStopSpectatingEntityEvent.getPlayer();
        var djPl = playerService.findByUuid(pl.getUniqueId());

        if (!djPl.getTeamMembersAlive().isEmpty()) {
            if (LocalDateTime.now().minusSeconds(2).isAfter(
                    lastMessageSend.getOrDefault(pl, LocalDateTime.now().minusSeconds(3)))) {
                var comp = text("You can not leave your target when your team mates are still alive!\n" +
                        "You can switch with /djspec <targetName> to another teammate.", RED);
                pl.sendMessage(comp);
                lastMessageSend.put(pl, LocalDateTime.now());
            }
            playerStopSpectatingEntityEvent.setCancelled(true);
        } else {
            djPl.setSpectateTarget(null);
            playerService.update(djPl);
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


    public static void tpAndSpectate(DjPlayer spec, DjPlayer target) {
        var sp = Bukkit.getPlayer(spec.getPlayerid());
        var targetSp = Bukkit.getPlayer(target.getPlayerid());
        if (targetSp != null && sp != null) {
            tpAndSpectate(sp, targetSp);
        }
    }

    public static void tpAndSpectate(Player spec, Player target) {
        spec.teleport(target);
        if (spec.getSpectatorTarget() == null || !spec.getSpectatorTarget().equals(target)) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(PaperDjBattlePlugin.getPlugin(), () -> {
                spec.setSpectatorTarget(target);
            }, 5);
        }
    }
}
