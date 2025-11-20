package net.jcom.minecraft.paperdjbattle.database.impl;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.table.TableUtils;
import net.jcom.minecraft.paperdjbattle.database.entities.DjPlayer;
import net.jcom.minecraft.paperdjbattle.database.entities.Team;

import java.sql.SQLException;
import java.util.UUID;

public class SqliteDatabase {

    private final Dao<DjPlayer, UUID> playerDao;
    private final Dao<Team, Integer> teamDao;

    public SqliteDatabase(String path) throws SQLException {
        var conn = new JdbcConnectionSource("jdbc:sqlite:%s".formatted(path));

        TableUtils.createTableIfNotExists(conn, Team.class);
        TableUtils.createTableIfNotExists(conn, DjPlayer.class);

        teamDao = DaoManager.createDao(conn, Team.class);
        playerDao = DaoManager.createDao(conn, DjPlayer.class);
    }

    public Dao<DjPlayer, UUID> getPlayerDao() {
        return playerDao;
    }

    public Dao<Team, Integer> getTeamDao() {
        return teamDao;
    }
}
