package me.botsko.prism.managers;

import me.botsko.prism.Prism;
import me.botsko.prism.players.PrismPlayer;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created for the Charlton IT Project.
 * Created by benjicharlton on 10/06/2019.
 */
public class PlayerManager {
    public static HashMap<UUID, PrismPlayer> prismPlayers = new HashMap<>();

    public static PrismPlayer cachePrismPlayer(final String playerName) {

        // Lookup the player
        PrismPlayer prismPlayer = getPrismPlayer(playerName);
        if (prismPlayer != null) {
            // prismPlayer = comparePlayerToCache( player, prismPlayer );
            Prism.debug("Loaded player " + prismPlayer.getName() + ", id: " + prismPlayer.getId() + " into the cache.");
            // Prism.prismPlayers.put( player.getUniqueId(), prismPlayer );
            return prismPlayer;
        }

        // Player is new, create a record for them
        prismPlayer = addPlayer(playerName);

        return prismPlayer;

    }
    /**
     * Converts UUID to a string ready for use against database
     *
     */
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

    public static String uuidToDbString(UUID id) {
        return id.toString().replace("-", "");
    }
    /**
     * Saves a real player's UUID and current Username to the `prism_players` table.
     * At this stage, we're pretty sure the UUID and username do not already exist.
     *
     * @param player
     */
    protected static PrismPlayer addPlayer(Player player) {
        String prefix = Prism.config.getString("prism.mysql.prefix");

        PrismPlayer prismPlayer = new PrismPlayer(0, player.getUniqueId(), player.getName());

        Connection conn = null;
        PreparedStatement s = null;
        ResultSet rs = null;
        try {

            conn = Prism.getPrismDataSource().getConnection();
            s = conn.prepareStatement("INSERT INTO " + prefix + "players (player,player_uuid) VALUES (?,UNHEX(?))",
                    Statement.RETURN_GENERATED_KEYS);
            s.setString(1, player.getName());
            s.setString(2, uuidToDbString(player.getUniqueId()));
            s.executeUpdate();

            rs = s.getGeneratedKeys();
            if (rs.next()) {
                prismPlayer.setId(rs.getInt(1));
                Prism.debug("Saved and loaded player " + player.getName() + " (" + player.getUniqueId()
                        + ") into the cache.");
                Prism.prismPlayers.put(player.getUniqueId(),
                        new PrismPlayer(rs.getInt(1), player.getUniqueId(), player.getName()));
            }
            else {
                throw new SQLException("Insert statement failed - no generated key obtained.");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException ignored) {
                }
            if (s != null)
                try {
                    s.close();
                } catch (SQLException ignored) {
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
        }
        return prismPlayer;
    }

    protected static PrismPlayer addPlayer(String playerName) {
        String prefix = Prism.config.getString("prism.mysql.prefix");

        PrismPlayer fakePlayer = new PrismPlayer(0, UUID.randomUUID(), playerName);

        Connection conn = null;
        PreparedStatement s = null;
        ResultSet rs = null;
        try {

            conn = Prism.getPrismDataSource().getConnection();
            s = conn.prepareStatement("INSERT INTO " + prefix + "players (player,player_uuid) VALUES (?,UNHEX(?))",
                    Statement.RETURN_GENERATED_KEYS);
            s.setString(1, fakePlayer.getName());
            s.setString(2, uuidToDbString(fakePlayer.getUUID()));
            s.executeUpdate();

            rs = s.getGeneratedKeys();
            if (rs.next()) {
                fakePlayer.setId(rs.getInt(1));
                Prism.debug("Saved and loaded fake player " + fakePlayer.getName() + " into the cache.");
                Prism.prismPlayers.put(fakePlayer.getUUID(), fakePlayer);
            }
            else {
                throw new SQLException("Insert statement failed - no generated key obtained.");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException ignored) {
                }
            if (s != null)
                try {
                    s.close();
                } catch (SQLException ignored) {
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
        }
        return fakePlayer;
    }
}
