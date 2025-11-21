package net.jcom.minecraft.paperdjbattle.database.entities;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "teams")
public class Team {
    public static final String ID_COL_NAME = "id";
    public static final String TEAMNAME_COL_NAME = "teamname";
    public static final String MEMBERS_COL_NAME = "members";
    public static final String ELIM_COL_NAME = "eliminated";

    @DatabaseField(generatedId = true, columnName = ID_COL_NAME)
    private int id;
    @DatabaseField(canBeNull = false, columnName = TEAMNAME_COL_NAME)
    private String teamname;
    @ForeignCollectionField(foreignFieldName = "team", columnName = MEMBERS_COL_NAME)
    private ForeignCollection<DjPlayer> teamMembers;
    @DatabaseField(defaultValue = "false", columnName = ELIM_COL_NAME)
    private boolean eliminated;

    public Team() {
    }

    public Team(String teamname) {
        this.teamname = teamname;
    }

    public int getId() {
        return id;
    }

    public String getTeamname() {
        return teamname;
    }

    public void setTeamname(String teamname) {
        this.teamname = teamname;
    }

    public ForeignCollection<DjPlayer> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(ForeignCollection<DjPlayer> teamMembers) {
        this.teamMembers = teamMembers;
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }
}
