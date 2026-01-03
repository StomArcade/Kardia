package net.bitbylogic.kardiavelocity.server.player;

import net.bitbylogic.kardia.util.RedisKeys;
import net.bitbylogic.kardiavelocity.KardiaVelocity;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class KardiaPlayer {

    private UUID uuid;
    private String name;
    private String server;

    public KardiaPlayer() {

    }

    public KardiaPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public KardiaPlayer(UUID uuid, String name, String server) {
        this.uuid = uuid;
        this.name = name;
        this.server = server;
    }

    public KardiaPlayer(UUID uuid, String server) {
        this.uuid = uuid;
        this.server = server;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public CompletionStage<Void> setName(@NotNull String newName) {
        RedissonClient redisson = KardiaVelocity.getInstance().getRedisManager().getRedissonClient();
        RMap<String, String> byName = redisson.getMap(RedisKeys.PLAYERS_BY_NAME);
        RMap<String, String> playerData = redisson.getMap(String.format(RedisKeys.PLAYERS, uuid));

        CompletableFuture<String> removeFuture = (this.name == null)
                ? CompletableFuture.completedFuture(null)
                : byName.removeAsync(this.name.toLowerCase()).toCompletableFuture();

        return removeFuture.thenRun(() -> {
            this.name = newName;
            byName.put(newName.toLowerCase(), uuid.toString());
        }).thenRun(() -> playerData.put("name", newName));
    }

    public String getServer() {
        return server;
    }

    public CompletionStage<Void> setServer(String server) {
        RedissonClient redisson = KardiaVelocity.getInstance().getRedisManager().getRedissonClient();
        RMap<String, String> playerData = redisson.getMap(String.format(RedisKeys.PLAYERS, uuid));

        return playerData.putAsync("server", server).thenApply(v -> null);
    }

}
