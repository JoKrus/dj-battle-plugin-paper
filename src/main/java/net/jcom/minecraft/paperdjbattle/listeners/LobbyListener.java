package net.jcom.minecraft.paperdjbattle.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class LobbyListener implements Listener {

    public LobbyListener(Plugin plugin) {
    }


    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent entityDamageEvent) {
        if (!(entityDamageEvent.getEntity() instanceof Player p)) {
            return;
        }

        if (p.getGameMode() == GameMode.ADVENTURE) {
            entityDamageEvent.setDamage(0);
        }
    }

    @EventHandler
    public void onPlayerHunger(FoodLevelChangeEvent foodLevelChangeEvent) {
        if (!(foodLevelChangeEvent.getEntity() instanceof Player p)) {
            return;
        }

        if (p.getGameMode() == GameMode.ADVENTURE) {
            foodLevelChangeEvent.setCancelled(true);
            p.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onHorseMount(EntityMountEvent entityMountEvent) {
        List<Entity> possPlayers = List.of(entityMountEvent.getMount(), entityMountEvent.getEntity());

        // SPIGOT FOR SOME REASON HAS THIS SHIT SWAPPED SOMETIMES
        Player p = null;
        for (var ent : possPlayers) {
            if (ent instanceof Player player) {
                p = player;
                break;
            }
        }
        if (p == null) {
            return;
        }

        if (p.getGameMode() == GameMode.ADVENTURE) {
            entityMountEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onHorseDamaged(EntityDamageByEntityEvent entityDamageEvent) {
        if (!(entityDamageEvent.getDamager() instanceof Player p)) {
            return;
        }

        if (entityDamageEvent.getEntity() instanceof Horse) {
            if (p.getGameMode() == GameMode.ADVENTURE) {
                entityDamageEvent.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void frameDrop(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player p) {
            if (p.getGameMode() == GameMode.ADVENTURE) {
                if (e.getEntity() instanceof ItemFrame) {
                    if (e.getDamager() instanceof Player) {
                        e.setCancelled(true);
                    }
                }
            }
        }

        if (e.getDamager() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player p) {
                if (p.getGameMode() == GameMode.ADVENTURE) {
                    e.getDamager().remove();
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void frameRotate(PlayerInteractEntityEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.ADVENTURE) {
            if (e.getRightClicked().getType().equals(EntityType.ITEM_FRAME)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onUnleash(PlayerUnleashEntityEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.ADVENTURE) {
            e.setCancelled(true);
        }
    }
}
