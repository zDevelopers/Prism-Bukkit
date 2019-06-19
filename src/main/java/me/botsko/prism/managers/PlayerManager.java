package me.botsko.prism.managers;

import me.botsko.prism.Prism;
import me.botsko.prism.players.PrismPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Created for the Charlton IT Project.
 * Created by benjicharlton on 10/06/2019.
 */
public class PlayerManager {
    private ExecutorService executor;
    private ExecutorCompletionService<PrismPlayer> service;
    private HashMap<Future<PrismPlayer>, Player> futurePlayerMap;
    public static HashMap<UUID, PrismPlayer> prismPlayers = new HashMap<>();

    public PlayerManager() {
        futurePlayerMap = new HashMap<>();
        executor = Executors.newCachedThreadPool();
        service = new ExecutorCompletionService<>(executor);
        Bukkit.getScheduler().runTaskAsynchronously(Prism.getInstance(), () -> {
            Prism.debug("Launching Aysnc Player Polling.");
            boolean running = true;
            while (running) {
                if (executor.isShutdown())
                    running = false;
                    try {
                        Future<PrismPlayer> f = service.take(); // this will block until a future is ready to be processed
                        PrismPlayer p = f.get();
                        Prism.debug("Loaded player " + p.getName() + ", id: " + p.getId() + " from the database.");
                        Player original = futurePlayerMap.get(f);
                        handlePrismPlayer(p, original);
                        futurePlayerMap.remove(f);
                    } catch (InterruptedException | ExecutionException e) {
                        Prism.log(e.getMessage());
                        if(Prism.getInstance().isDebug())
                            e.printStackTrace();
                    }
            }
            Prism.debug("Terminated Aysnc Player Polling.");
        });
    }

    public void cacheAllOnlinePlayer() {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        players.forEach((Consumer<Player>) player -> {
            Future<PrismPlayer> playerFuture = getPrismPlayer(player);
            if (playerFuture.isDone()) {
                try {
                    prismPlayers.put(player.getUniqueId(), playerFuture.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Future<PrismPlayer> cachePrismPlayer(final String player) {
        List<Player> online = Bukkit.matchPlayer(player);
        Future<PrismPlayer> pFuture;
        Player original;
        boolean isOnline = false;
        if (online.size() == 1) {
            original = online.get(0);
            pFuture = getPrismPlayer(original);
            isOnline = true;
        } else if (online.isEmpty()) {
            pFuture = getPrismPlayer(player);
            original = null;
        } else {
            Prism.log("Ambigous Value for " + player + " corresponded to multiple online players!");
            return CompletableFuture.completedFuture(null);
        }
        if (pFuture.isDone()) {
            try {
                PrismPlayer prismPlayer = pFuture.get();
                if (isOnline) {
                    handlePrismPlayer(prismPlayer, original);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return pFuture;
    }

    public Future<PrismPlayer> cachePrismPlayer(final Player player) {
        Future<PrismPlayer> pFuture = getPrismPlayer(player);
        if (pFuture.isDone()) {
            try {
                PrismPlayer prismPlayer = pFuture.get();
                handlePrismPlayer(prismPlayer, player);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return pFuture;
    }

    private void handlePrismPlayer(PrismPlayer prismPlayer, Player original) {
        if (prismPlayer != null) {
            comparePlayerToCache(original, prismPlayer);
            Prism.prismPlayers.put(prismPlayer.getUUID(), prismPlayer);
            return;
        }
        if(original == null)
            return;
        // Player is new and not null create a record for them
        addPlayer(original);
    }

    private void comparePlayerToCache(Player player, PrismPlayer prismPlayer) {
        if (player == null) {
            Prism.log("Player is null for " + prismPlayer.getName());
            return;
        }
        // Compare for username differences, update database
        if (!player.getName().equals(prismPlayer.getName())) {
            prismPlayer.setName(player.getName());
            updatePlayer(prismPlayer);
        }

        // Compare UUID
        if (!player.getUniqueId().equals(prismPlayer.getUUID())) {
            Prism.log("Player UUID for " + player.getName() + " does not match our cache! " + player.getUniqueId()
                    + " versus cache of " + prismPlayer.getUUID());
            // Update anyway...
            prismPlayer.setUUID(player.getUniqueId());
            updatePlayer(prismPlayer);

        }
    }
    @NotNull
    private void addPlayer(Player player) {
        Future<PrismPlayer> fut = service.submit(Prism.getPrismDataSource().getPlayerQuery().addPlayer(player));
        futurePlayerMap.put(fut, player);
    }

    /**
     * Returns a `prism_players` ID for the described player name. If one cannot be
     * found, returns 0.
     * <p>
     * Used by the recorder in determining proper foreign key
     *
     * @param
     * @return Future<PrismPlayer>
     */
    public Future<PrismPlayer> getPrismPlayer(String playerName) {

        Player player = Bukkit.getPlayer(playerName);
        if (player != null)
            return getPrismPlayer(player);
        // Player not online, we need to go to cache
        return service.submit(Prism.getPrismDataSource().getPlayerQuery().lookupPlayerbyName(playerName));
    }

    /**
     * Returns a `prism_players` ID for the described player object. If one cannot
     * be found, returns 0.
     *
     * Used by the recorder in determining proper foreign key
     *
     * @return
     */
    public Future<PrismPlayer> getPrismPlayer(Player player) {
        // Are they in the cache?
        PrismPlayer prismPlayer = Prism.prismPlayers.get(player.getUniqueId());
        if (prismPlayer != null) {
            Prism.debug("Loaded player " + player.getName() + ", id: " + prismPlayer.getId() + " from the cache.");
            return CompletableFuture.completedFuture(prismPlayer);
        }
        // Lookup by UUID
        Prism.debug("Loading player " + player.getName() + " into the cache.");
        Future<PrismPlayer> f = service.submit(Prism.getPrismDataSource().getPlayerQuery().lookupPlayerbyUUID(player.getUniqueId()));
        futurePlayerMap.put(f, player);
        return f;
    }

    protected void updatePlayer(PrismPlayer prismPlayer) {
        Prism.debug("Updating "+prismPlayer.getName()  + " in the database");
        executor.submit(() -> Prism.getPrismDataSource().getPlayerQuery().updatePlayer(prismPlayer));
    }

    protected void addPlayer(String playerName) {
        Future<PrismPlayer> fut = service.submit(Prism.getPrismDataSource().getPlayerQuery().addPlayer(playerName));
        futurePlayerMap.put(fut, null);
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

    private class ComparitivePlayer extends CompletableFuture<PrismPlayer> {
        protected Player original;
    }
}
