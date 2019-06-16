package me.botsko.prism.database;

import me.botsko.prism.players.PrismPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 10/06/2019.
 */
public interface PlayerQuery {

    Callable<PrismPlayer> lookupPlayerbyUUID(UUID uuid);

    Callable<PrismPlayer> lookupPlayerbyName(String name);

    Callable<PrismPlayer> addPlayer(Player player);

    /**
     * This should be checked that its never used to add real players.
     *
     * @param playerName
     * @return
     */
    Callable<PrismPlayer> addPlayer(String playerName);

    void updatePlayer(PrismPlayer pPlayer);

}
