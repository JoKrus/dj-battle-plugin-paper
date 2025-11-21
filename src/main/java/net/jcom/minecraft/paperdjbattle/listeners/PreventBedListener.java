package net.jcom.minecraft.paperdjbattle.listeners;

import org.bukkit.block.data.type.Bed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class PreventBedListener implements Listener {

    public PreventBedListener(Plugin plugin) {
    }

    @EventHandler
    public void preventBedPlaced(BlockPlaceEvent blockPlaceEvent) {
        if (blockPlaceEvent.getBlockPlaced().getBlockData() instanceof Bed) {
            blockPlaceEvent.setCancelled(true);
            blockPlaceEvent.getPlayer().sendMessage(text("Beds are not allowed to be placed.", RED));
        }
    }
}
