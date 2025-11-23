package net.jcom.minecraft.paperdjbattle.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class GracePeriodListener implements Listener {
    public GracePeriodListener() {
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent entityDamageEvent) {
        if (!(entityDamageEvent.getEntity() instanceof Player p)) {
            return;
        }

        entityDamageEvent.setCancelled(true);
    }
}
