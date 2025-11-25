package net.jcom.minecraft.paperdjbattle.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.jcom.minecraft.paperdjbattle.listeners.BattleHandler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@DatabaseTable(tableName = "players")
public class DjPlayer {
    public static final String ID_COL_NAME = "playerid";
    public static final String NAME_COL_NAME = "name";
    public static final String TEAM_COL_NAME = "team_id";
    public static final String SPEC_COL_NAME = "spectate_target";


    @DatabaseField(id = true, columnName = ID_COL_NAME)
    private UUID playerid;
    @DatabaseField(canBeNull = false, columnName = NAME_COL_NAME)
    private String name;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = TEAM_COL_NAME)
    private Team team;
    @DatabaseField(canBeNull = true, foreign = true, foreignAutoRefresh = true, columnName = SPEC_COL_NAME)
    private DjPlayer spectateTarget;

    public DjPlayer() {
    }

    public DjPlayer(Player player) {
        this.playerid = player.getUniqueId();
        this.name = player.getName();
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public List<DjPlayer> getTeamMembersAlive() {
        if (team == null) {
            return new ArrayList<>();
        }
        var ret = new ArrayList<DjPlayer>();
        for (var player : team.getTeamMembers()) {
            if (player.getPlayerid().equals(this.getPlayerid())) {
                continue;
            }
            var bukkitPlayer = Bukkit.getPlayer(player.getPlayerid());
            if (bukkitPlayer != null && bukkitPlayer.getGameMode() == GameMode.SURVIVAL && bukkitPlayer.getHealth() > 0) {
                ret.add(player);
            }
        }
        return ret;
    }

    public UUID getPlayerid() {
        return playerid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DjPlayer getSpectateTarget() {
        return spectateTarget;
    }

    public void setSpectateTarget(DjPlayer spectateTarget) {
        this.spectateTarget = spectateTarget;
        if (spectateTarget != null) {
            BattleHandler.tpAndSpectate(this, spectateTarget);
        }
    }
}
