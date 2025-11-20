package net.jcom.minecraft.paperdjbattle.database.services;

import com.j256.ormlite.dao.Dao;
import net.jcom.minecraft.paperdjbattle.database.entities.DjPlayer;

import java.util.List;
import java.util.UUID;

public class PlayerService {

    private final Dao<DjPlayer, UUID> playerDao;

    public PlayerService(Dao<DjPlayer, UUID> playerDao) {
        this.playerDao = playerDao;
    }

    public DjPlayer create(DjPlayer player) {
        try {
            playerDao.create(player);
            return player;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public DjPlayer findByUuid(UUID uuid) {
        try {
            return playerDao.queryForId(uuid);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<DjPlayer> findAll() {
        try {
            return playerDao.queryForAll();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<DjPlayer> findTeamless() {
        try {
            return playerDao.queryBuilder().where().isNull(DjPlayer.TEAM_COL_NAME).query();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public DjPlayer update(DjPlayer player) {
        try {
            playerDao.update(player);
            return player;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean existsByUuid(UUID uuid) {
        try {
            return playerDao.idExists(uuid);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
