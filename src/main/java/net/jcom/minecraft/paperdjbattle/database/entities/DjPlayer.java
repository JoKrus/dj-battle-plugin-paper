package net.jcom.minecraft.paperdjbattle.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.bukkit.entity.Player;

import java.util.UUID;

@DatabaseTable(tableName = "players")
public class DjPlayer {
    public static final String ID_COL_NAME = "playerid";
    public static final String NAME_COL_NAME = "name";
    public static final String TEAM_COL_NAME = "team_id";


    @DatabaseField(id = true, columnName = ID_COL_NAME)
    private UUID playerid;
    @DatabaseField(canBeNull = false, columnName = NAME_COL_NAME)
    private String name;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = TEAM_COL_NAME)
    private Team team;

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

    public UUID getPlayerid() {
        return playerid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
