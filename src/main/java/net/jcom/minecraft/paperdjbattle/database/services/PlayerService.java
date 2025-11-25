package net.jcom.minecraft.paperdjbattle.database.services;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import net.jcom.minecraft.paperdjbattle.database.entities.DjPlayer;

import java.util.List;
import java.util.UUID;

import static net.jcom.minecraft.paperdjbattle.database.entities.DjPlayer.*;

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
            return List.of();
        }
    }

    public List<DjPlayer> findTeamless() {
        try {
            return playerDao.queryBuilder().where().isNull(TEAM_COL_NAME).query();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }


    public List<DjPlayer> findNotMatching(List<UUID> uuids) {
        try {
            return playerDao.queryBuilder().where().notIn(ID_COL_NAME, uuids).query();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<DjPlayer> findAllSpectators(UUID uuid) {
        try {
            return playerDao.queryBuilder().where().eq(SPEC_COL_NAME, uuid).query();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<DjPlayer> findAllSpectatingTeammates(UUID uuid) {
        //TODO possible bugs
        try {
            //return playerDao.queryBuilder().where().eq(DjPlayer.SPEC_COL_NAME, uuid).and().eq.query();
            // Main query builder (players who might be spectating)
            QueryBuilder<DjPlayer, UUID> qb = playerDao.queryBuilder();

            // Sub-query builder to fetch the target player's team
            QueryBuilder<DjPlayer, UUID> sub = playerDao.queryBuilder();
            sub.selectColumns(TEAM_COL_NAME)
                    .where().eq(ID_COL_NAME, uuid);

            // Final query: same team AND spectate_target = targetId
            qb.where()
                    .eq(SPEC_COL_NAME, uuid)
                    .and()
                    .in(TEAM_COL_NAME, sub);

            return qb.query();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public int deleteNotMatching(List<UUID> uuids) {
        try {
            var deleteBuild = playerDao.deleteBuilder();
            deleteBuild.where().notIn(ID_COL_NAME, uuids);
            return deleteBuild.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int deleteTeamless() {
        try {
            var deleteBuild = playerDao.deleteBuilder();
            deleteBuild.where().isNull(TEAM_COL_NAME);
            return deleteBuild.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
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
