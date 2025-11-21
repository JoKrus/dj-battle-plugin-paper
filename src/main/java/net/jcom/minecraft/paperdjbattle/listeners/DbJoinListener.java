package net.jcom.minecraft.paperdjbattle.listeners;

import net.jcom.minecraft.paperdjbattle.database.entities.DjPlayer;
import net.jcom.minecraft.paperdjbattle.database.services.PlayerService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class DbJoinListener implements Listener {
    private final PlayerService playerService;

    public DbJoinListener(PlayerService playerService) {
        this.playerService = playerService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        var djPlayer = playerService.findByUuid(player.getUniqueId());
        if (djPlayer == null) {
            var newDjPlayer = new DjPlayer(player);
            playerService.create(newDjPlayer);
        } else if (!player.getName().equals(djPlayer.getName())) {
            djPlayer.setName(player.getName());
            playerService.update(djPlayer);
        }
    }
}
