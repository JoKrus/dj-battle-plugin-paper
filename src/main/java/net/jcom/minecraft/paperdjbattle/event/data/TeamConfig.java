package net.jcom.minecraft.paperdjbattle.event.data;

import net.jcom.minecraft.paperdjbattle.database.entities.DjPlayer;
import net.jcom.minecraft.paperdjbattle.database.entities.Team;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.List;

public class TeamConfig {
    /**
     * Map that contains the teamName as key and a list of all members as value
     */
    public final HashMap<String, List<OfflinePlayer>> teamToPlayers = new HashMap<>();
    public static final TeamConfig EMPTY = new TeamConfig();

    private TeamConfig() {
    }

    public static TeamConfig getConfig(List<Team> allTeams, List<DjPlayer> players) {
        var config = new TeamConfig();
        for (Team team : allTeams) {
            var members = team.getTeamMembers();
            var memberList = members.stream().map(player -> Bukkit.getOfflinePlayer(player.getPlayerid())).toList();
            config.teamToPlayers.put(team.getTeamname(), memberList);
        }
        return config;
    }
}
