package net.jcom.minecraft.paperdjbattle.database.services;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.misc.TransactionManager;
import net.jcom.minecraft.paperdjbattle.config.Defaults;
import net.jcom.minecraft.paperdjbattle.config.DefaultsManager;
import net.jcom.minecraft.paperdjbattle.database.entities.DjPlayer;
import net.jcom.minecraft.paperdjbattle.database.entities.Team;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class TeamService {
    private final Dao<Team, Integer> teamDao;
    private final Dao<DjPlayer, UUID> playerDao;

    public TeamService(Dao<Team, Integer> teamDao, Dao<DjPlayer, UUID> playerDao) {
        this.teamDao = teamDao;
        this.playerDao = playerDao;
    }

    public Team create(Team team) {
        try {
            teamDao.create(team);
            return team;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Team createOrJoinTeam(DjPlayer player, String name) throws SQLException {
        return TransactionManager.callInTransaction(teamDao.getConnectionSource(), () -> {

            Team team = findByName(name);
            if (team != null) {
                if (team.getTeamMembers().size() >= DefaultsManager.<Integer>getValue(Defaults.TEAM_SIZE)) {
                    return null;
                }
            } else {
                team = create(new Team(name));
            }

            //add the player
            player.setTeam(team);
            playerDao.update(player);

            return team;
        });
    }

    public void leaveOrDeleteTeam(DjPlayer player) throws SQLException {
        TransactionManager.callInTransaction(teamDao.getConnectionSource(), () -> {
            var team = player.getTeam();
            player.setTeam(null);
            playerDao.update(player);

            if (team.getTeamMembers().isEmpty()) {
                teamDao.delete(team);
            }
            return true;
        });
    }

    public boolean removeTeam(Team team) {
        try {
            return TransactionManager.callInTransaction(teamDao.getConnectionSource(), () -> {
                var players = team.getTeamMembers();

                for (var pl : players) {
                    pl.setTeam(null);
                    playerDao.update(pl);
                }

                teamDao.delete(team);
                return true;
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Team findByName(String name) {
        try {
            return teamDao.queryBuilder().where()
                    .eq(Team.TEAMNAME_COL_NAME, name).queryForFirst();
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public Team findById(int id) {
        try {
            return teamDao.queryForId(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Team> findAll() {
        try {
            return teamDao.queryForAll();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<Team> findAllSorted() {
        try {
            var list = teamDao.queryForAll();
            list.sort(Comparator.comparing(Team::getTeamname));
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public Team update(Team team) {
        try {
            teamDao.update(team);
            return team;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteById(int id) {
        try {
            teamDao.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ForeignCollection<DjPlayer> getTeamMembers(int teamId) {
        try {
            var team = teamDao.queryForId(teamId);
            if (team == null) return null;
            return team.getTeamMembers();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
