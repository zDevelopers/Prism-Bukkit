package me.botsko.prism.database.sql;

import me.botsko.prism.Prism;
import me.botsko.prism.database.PlayerQuery;
import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.database.AbstractFuturePlayer;
import me.botsko.prism.players.PrismPlayer;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 10/06/2019.
 */
public class SQLPlayerQuery implements PlayerQuery {
    private PrismDataSource dataSource;

    /**
     * @param dataSource
     */
    public SQLPlayerQuery(PrismDataSource dataSource) {
        this.dataSource = dataSource;
    }


    protected String select() {
        return "SELECT player_id, player, HEX(player_uuid)";
    }

    protected String where(boolean byUUID) {
        if (byUUID)
            return "player_uuid = UNHEX(?)";
        else {
            return "WHERE player = ?";
        }
    }

    @Override
    public Callable<PrismPlayer> lookupPlayerbyUUID(UUID uuid) {
        return new AbstractFuturePlayer() {
            @Override
            public PrismPlayer call() throws InterruptedException, ExecutionException {
                try (Connection conn = dataSource.getConnection()) {
                    s = conn.prepareStatement(getQuery(true));
                    s.setString(1, uuidToDbString(uuid));
                    ResultSet rs = s.executeQuery();
                    return handleResult(rs);
                } catch (SQLException e) {
                    throw new ExecutionException(e);
                }
            }
        };
    }

    @Override
    public Callable<PrismPlayer> lookupPlayerbyName(String name) {
        return new AbstractFuturePlayer() {
            @Override
            public PrismPlayer call() throws InterruptedException, ExecutionException {
                try (Connection conn = dataSource.getConnection()) {
                    s = conn.prepareStatement(getQuery(false));
                    s.setString(1, name);
                    ResultSet rs = s.executeQuery();
                    return handleResult(rs);
                } catch (SQLException e) {
                    throw new ExecutionException(e);
                }
            }
        };
    }

    public Callable<PrismPlayer> addPlayer(Player player) {
        return new AbstractFuturePlayer() {
            @Override
            public PrismPlayer call() throws Exception {
                PrismPlayer prismPlayer = new PrismPlayer(0, player.getUniqueId(), player.getName());
                return insertPrismPlayer(prismPlayer);
            }
        };
    }

    @Override

    public Callable<PrismPlayer> addPlayer(String playerName) {
        return new AbstractFuturePlayer() {
            @Override
            public PrismPlayer call() throws Exception {
                PrismPlayer fakePlayer = new PrismPlayer(0, UUID.randomUUID(), playerName);
                return insertPrismPlayer(fakePlayer);
            }
        };

    }

    private PrismPlayer insertPrismPlayer(PrismPlayer prismPlayer) {
        ResultSet rs = null;
        String prefix = Prism.config.getString("prism.mysql.prefix");
        String updateQuery = "INSERT INTO " + prefix + "players (player,player_uuid) VALUES (?,UNHEX(?))";
        try (
                Connection conn = Prism.getPrismDataSource().getConnection();
                PreparedStatement s = conn.prepareStatement(updateQuery, Statement.RETURN_GENERATED_KEYS);
        ) {
            s.setString(1, prismPlayer.getName());
            s.setString(2, uuidToDbString(prismPlayer.getUUID()));
            s.executeUpdate();

            rs = s.getGeneratedKeys();
            if (rs.next()) {
                prismPlayer.setId(rs.getInt(1));
                Prism.debug("Saved and loaded player " + prismPlayer.getName() + " (" + prismPlayer.getUUID()
                        + ") into the cache.");
                Prism.prismPlayers.put(prismPlayer.getUUID(), prismPlayer);
            } else {
                throw new SQLException("Insert statement failed - no generated key obtained.");
            }
        } catch (SQLException e) {
            Prism.getPrismDataSource().handleDataSourceException(e);
            return null;
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (SQLException e) {
                Prism.getPrismDataSource().handleDataSourceException(e);
            }
        }
        return prismPlayer;
    }

    @Override
    public void updatePlayer(PrismPlayer pPlayer) {
        String prefix = Prism.config.getString("prism.mysql.prefix");
        String query = "UPDATE " + prefix + "players SET player = ?, player_uuid = UNHEX(?) WHERE player_id = ?";
        try (
                Connection conn = Prism.getPrismDataSource().getConnection();
                PreparedStatement s = conn.prepareStatement(query);
        ) {
            s.setString(1, pPlayer.getName());
            s.setString(2, uuidToDbString(pPlayer.getUUID()));
            s.setInt(3, pPlayer.getId());
            s.executeUpdate();

        } catch (SQLException e) {
            Prism.getPrismDataSource().handleDataSourceException(e);
        }
    }

    public static String uuidToDbString(UUID id) {
        return id.toString().replace("-", "");
    }

    public static UUID uuidFromDbString(String uuid) {
        // Positions need to be -2
        String completeUuid = uuid.substring(0, 8);
        completeUuid += "-" + uuid.substring(8, 12);
        completeUuid += "-" + uuid.substring(12, 16);
        completeUuid += "-" + uuid.substring(16, 20);
        completeUuid += "-" + uuid.substring(20, uuid.length());
        completeUuid = completeUuid.toLowerCase();
        return UUID.fromString(completeUuid);
    }

    public String getQuery(boolean byUUID) {

        String query = select() + where(byUUID) + " LIMIT 1";

        query += ";";
        dataSource.getLog().debug(query);
        return query;

    }
}