package net.jcom.minecraft.paperdjbattle.listeners;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PreBattleListener implements Listener {


    @EventHandler
    public void onPlayerJump(PlayerJumpEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerAttack(PrePlayerAttackEntityEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        // allow small movement and looking around
        if (!e.hasChangedBlock()) {
            return;
        }

        Location from = e.getFrom();
        Location to = e.getTo();

        // Cancel unless player moves straight down in place
        if (to.getBlockY() >= from.getBlockY() ||
                to.getBlockX() != from.getBlockX() ||
                to.getBlockZ() != from.getBlockZ()) {
            e.setCancelled(true);
        }
    }
}
