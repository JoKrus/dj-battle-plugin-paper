package net.jcom.minecraft.paperdjbattle.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.jcom.minecraft.paperdjbattle.PaperDjBattlePlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class ChatListener implements Listener {

    public ChatListener(Plugin plugin) {
    }

    @EventHandler
    public void onPlayerChatPaper(AsyncChatEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.SPECTATOR) {
            return;
        }
        event.setCancelled(true);
        //If spectator, only sent to spectators
        var comp = text().append(text("[DEAD]", RED)).append(text(" <"))
                .append(text(event.getPlayer().getName())).append(text("> "))
                .append(event.message());

        Bukkit.getScheduler().runTask(PaperDjBattlePlugin.getPlugin(), () -> {
            for (var pl : Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.getGameMode() == GameMode.SPECTATOR)
                    .toList()) {
                pl.sendMessage(comp);
            }
        });
    }
}
