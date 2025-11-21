package net.jcom.minecraft.paperdjbattle.listeners;

import net.jcom.minecraft.paperdjbattle.config.BattleState;
import net.jcom.minecraft.paperdjbattle.config.BattleStateManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class PlayerCommandSendListener implements Listener {
    private final Plugin plugin;

    public PlayerCommandSendListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommandSend(PlayerCommandPreprocessEvent playerCommandPreprocessEvent) {
        String cmd = playerCommandPreprocessEvent.getMessage().substring(1);
        if (BattleStateManager.get().getState() != BattleState.LOBBY) {
            List<String> prefixes = List.of("djspec", "djteam list", "djbattle stop", "say");

            boolean startsWithPrefix = false;
            for (String prefix : prefixes) {
                if (cmd.startsWith(prefix)) {
                    startsWithPrefix = true;
                    break;
                }
            }

            if (!startsWithPrefix) {
                playerCommandPreprocessEvent.setCancelled(true);
                playerCommandPreprocessEvent.getPlayer().sendMessage(
                        text(playerCommandPreprocessEvent.getMessage()
                                + " was stopped because a battle is going on.", RED));
                plugin.getLogger().info(cmd + " was stopped because a battle is going on.");
                return;
            }
        } else {
            var forbiddenStrings = List.of("warp");

            if (!playerCommandPreprocessEvent.getPlayer().isOp()) {
                if (stringInList(forbiddenStrings, cmd.split(" ")[0])) {
                    playerCommandPreprocessEvent.setCancelled(true);
                    playerCommandPreprocessEvent.getPlayer().sendMessage(
                            text(playerCommandPreprocessEvent.getMessage()
                                    + " was stopped because it contains forbidden command words."));
                    plugin.getLogger().info(cmd + " was stopped because it contains forbidden command words.");
                }
            }
        }
    }

    private static boolean stringInList(List<String> stringList, String s) {
        for (var sFromList : stringList) {
            if (sFromList.contains(s)) return true;
        }
        return false;
    }

}
