package net.jcom.minecraft.paperdjbattle.listeners;

import net.jcom.minecraft.paperdjbattle.database.entities.DjPlayer;
import net.jcom.minecraft.paperdjbattle.database.services.PlayerService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final PlayerService playerService;

    public JoinListener(PlayerService playerService) {
        this.playerService = playerService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        if (playerService.findByUuid(player.getUniqueId()) == null) {
            var djPlayer = new DjPlayer(player);
            playerService.create(djPlayer);
        }
    }
}
