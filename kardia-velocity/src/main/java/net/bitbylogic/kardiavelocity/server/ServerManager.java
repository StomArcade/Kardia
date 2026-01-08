package net.bitbylogic.kardiavelocity.server;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.bitbylogic.kardia.server.KardiaServer;
import net.bitbylogic.kardia.server.ServerType;
import net.bitbylogic.kardia.util.RedisKeys;
import net.bitbylogic.kardiavelocity.KardiaVelocity;
import net.bitbylogic.kardiavelocity.server.player.KardiaPlayer;
import net.bitbylogic.kardiavelocity.server.settings.ServerSettings;
import net.bitbylogic.kardiavelocity.util.message.MessageUtil;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class ServerManager {

    private final Gson gson = new Gson();

    private final List<KardiaServer> cachedServers = new ArrayList<>();

    private String serverName;

    private ServerType serverType;

    private ServerSettings serverSettings;
    private ServerEnvironment serverEnvironment;

    private final KardiaVelocity plugin;
    private final ProxyServer proxyServer;

    public ServerManager(@NotNull KardiaVelocity plugin, @NotNull ProxyServer proxyServer) {
        this.plugin = plugin;
        this.proxyServer = proxyServer;

        this.serverType = null;
        this.serverSettings = new ServerSettings();
        this.serverEnvironment = new ServerEnvironment();
        this.serverName = serverEnvironment.getEnv(ServerEnvironment.EnvVariable.KARDIA_ID);

        serverSettings.setJoinState(KardiaServer.JoinState.JOINABLE);
    }

    public void start() {
        proxyServer.getScheduler().buildTask(plugin, () -> {
            for (Player player : proxyServer.getAllPlayers()) {
                if(player.getCurrentServer().isPresent() && !player.getCurrentServer().get().getServerInfo().getName().equalsIgnoreCase("fallback")) {
                    continue;
                }

                KardiaServer server = KardiaVelocity.getInstance()
                        .getServerManager()
                        .getPriorityServerById(KardiaVelocity.getInstance().getServerManager().environment().getEnv("LOBBY_ID", "fallback"));

                if (server == null || server.joinState() != KardiaServer.JoinState.JOINABLE) {
                    continue;
                }

                connectPlayer(player, server);
            }
        }).repeat(Duration.ofSeconds(1)).schedule();

        proxyServer.getScheduler().buildTask(plugin, () -> {
            RedissonClient redissonClient = plugin.getRedisManager().getRedissonClient();
            RMap<String, String> servers = redissonClient.getMap(RedisKeys.SERVERS);

            if(servers != null) {
                this.cachedServers.clear();

                this.cachedServers.addAll(servers.values().stream()
                        .map(s -> gson.fromJson(s, KardiaServer.class))
                        .filter(Objects::nonNull)
                        .toList());

                String kardiaId = serverEnvironment.getEnv(ServerEnvironment.EnvVariable.KARDIA_ID);
                servers.getAsync(kardiaId).thenAccept(value -> {
                    if (value != null) {
                        KardiaServer kardiaServer = gson.fromJson(value, KardiaServer.class);

                        if (serverType == null) {
                            serverType = kardiaServer.serverType();
                        }

                        KardiaServer modifiedServer = new KardiaServer(
                                kardiaServer.dockerId(),
                                kardiaServer.kardiaId(),
                                kardiaServer.instance(),
                                kardiaServer.ids(),
                                kardiaServer.serverType(),
                                kardiaServer.ip(),
                                kardiaServer.boundPort(),
                                serverSettings.maxPlayers(),
                                serverSettings.joinState(),
                                serverSettings.isPrivateServer(),
                                proxyServer.getAllPlayers().stream().map(Player::getUniqueId).toList()
                        );

                        servers.putAsync(kardiaId, gson.toJson(modifiedServer));
                    }
                });

                rebuildProxyServerCache();
            }
        }).repeat(Duration.ofSeconds(10)).schedule();
    }

    public List<KardiaServer> getServersByInstance(String instance) {
        return this.cachedServers.stream()
                .filter(server -> server.instance().equals(instance))
                .collect(Collectors.toList());
    }

    public List<KardiaServer> getServersById(String id) {
        return this.cachedServers.stream()
                .filter(server -> server.ids().contains(id))
                .collect(Collectors.toList());
    }

    public KardiaServer getPriorityServerByInstance(String instance) {
        List<KardiaServer> servers = getServersByInstance(instance);
        CompletableFuture<KardiaServer> priority = new CompletableFuture<>();
        servers.forEach(server -> {
            if(server.joinState() == KardiaServer.JoinState.JOINABLE && (priority.getNow(null) == null ||
                    server.players().size() > priority.getNow(null).players().size())) {
                priority.complete(server);
            }
        });
        return priority.getNow(null);
    }

    public KardiaServer getPriorityServerById(String id) {
        List<KardiaServer> servers = getServersById(id);
        CompletableFuture<KardiaServer> priority = new CompletableFuture<>();
        servers.forEach(server -> {
            if(server.joinState() == KardiaServer.JoinState.JOINABLE && ((priority.getNow(null) == null ||
                    (server.players().size() > priority.getNow(null).players().size())))) {
                priority.complete(server);
            }
        });
        return priority.getNow(null);
    }

    public ServerInfo constructServerInfo(KardiaServer server) {
        return new ServerInfo(server.kardiaId(),
                new InetSocketAddress(serverType != null ? "host.docker.internal" :
                        proxyServer.getBoundAddress().getHostString(), server.boundPort()));
    }

    public void rebuildProxyServerCache() {
        List<ServerInfo> toRemove = Lists.newArrayList();

        proxyServer.getAllServers().forEach(server -> {
            if(!server.getServerInfo().getName().equalsIgnoreCase("fallback"))
                toRemove.add(server.getServerInfo());
        });

        toRemove.forEach(proxyServer::unregisterServer);

        this.cachedServers.forEach(server -> {
            if(server.serverType() != null && server.serverType() != ServerType.PROXY)
                proxyServer.registerServer(constructServerInfo(server));
        });
    }

    public List<KardiaServer> getCachedServers() {
        return cachedServers;
    }

    public void connectPlayer(Player player, String id, boolean notify) {
        KardiaServer server = getPriorityServerById(id);
        if(server != null) {
            connectPlayer(player, server, notify);
        } else {
            player.disconnect(Component.text("§cCould not find a server to connect you to!"));
        }
    }

    public void connectPlayer(Player player, KardiaServer server) {
        connectPlayer(player, server, true);
    }

    public void connectPlayer(Player player, KardiaServer server, boolean notify) {
        ServerInfo info = constructServerInfo(server);
        proxyServer.getServer(info.getName()).ifPresent(registeredServer -> player.createConnectionRequest(registeredServer).connect());

        if(notify) {
            player.sendMessage(MessageUtil.success("Connecting you to <success_highlight><smallcaps>" + server.kardiaId() + "<success_secondary>..."));
        }
    }

    public CompletionStage<KardiaPlayer> getPlayer(UUID uuid) {
        RedissonClient redisson = plugin.getRedisManager().getRedissonClient();
        RMap<String, String> playerData = redisson.getMap(String.format(RedisKeys.PLAYERS, uuid));

        return playerData.readAllMapAsync()
                .thenApply(data -> {
                    if (data.isEmpty()) {
                        return null;
                    }

                    return new KardiaPlayer(uuid, data.get("name"), data.get("server"));
                });
    }

    public CompletionStage<KardiaPlayer> getPlayer(String name) {
        RedissonClient redisson = plugin.getRedisManager().getRedissonClient();
        RMap<String, String> byName = redisson.getMap(RedisKeys.PLAYERS_BY_NAME);

        String normalizedName = name.toLowerCase();

        return byName.getAsync(normalizedName)
                .thenCompose(uuidStr -> {
                    if (uuidStr == null) {
                        return CompletableFuture.completedFuture(null);
                    }

                    return getPlayer(UUID.fromString(uuidStr));
                });
    }

    public void addPlayer(KardiaPlayer player) {
        updatePlayer(player);
    }

    public CompletionStage<Boolean> removePlayer(KardiaPlayer player) {
        RedissonClient redisson = plugin.getRedisManager().getRedissonClient();
        RMap<String, String> byName = redisson.getMap(RedisKeys.PLAYERS_BY_NAME);
        RMap<String, String> playerData = redisson.getMap(String.format(RedisKeys.PLAYERS, player.getUuid()));

        return byName.removeAsync(player.getName().toLowerCase()).thenCompose(name -> playerData.deleteAsync());
    }

    public CompletionStage<Void> updatePlayer(@NotNull KardiaPlayer player) {
        RedissonClient redisson = plugin.getRedisManager().getRedissonClient();
        RMap<String, String> byName = redisson.getMap(RedisKeys.PLAYERS_BY_NAME);
        RMap<String, String> playerData = redisson.getMap(String.format(RedisKeys.PLAYERS, player.getUuid()));

        return playerData.getAsync("name").thenCompose(oldName -> {
            CompletableFuture<String> removeFuture = (oldName != null && !oldName.equals(player.getName()))
                    ? byName.removeAsync(oldName.toLowerCase()).toCompletableFuture()
                    : CompletableFuture.completedFuture(null);

            return removeFuture.thenCompose(v -> {
                CompletableFuture<String> byNameFuture = byName.putAsync(player.getName().toLowerCase(), player.getUuid().toString())
                        .toCompletableFuture();

                CompletableFuture<String> playerDataFuture = playerData.putAsync("name", player.getName())
                        .thenCompose(server -> playerData.putAsync("server", player.getServer()))
                        .toCompletableFuture();

                return CompletableFuture.allOf(byNameFuture, playerDataFuture);
            });
        });
    }

    public CompletableFuture<List<KardiaPlayer>> getAllPlayers() {
        RedissonClient redisson = plugin.getRedisManager().getRedissonClient();
        RMap<String, String> byName = redisson.getMap(RedisKeys.PLAYERS_BY_NAME);

        return byName.readAllMapAsync().thenCompose(nameToUuid -> {
            List<CompletableFuture<KardiaPlayer>> futures = new ArrayList<>();

            nameToUuid.values().forEach(uuidStr -> {
                UUID uuid = UUID.fromString(uuidStr);
                RMap<String, String> playerData = redisson.getMap(String.format(RedisKeys.PLAYERS, uuid));
                CompletableFuture<KardiaPlayer> playerFuture = playerData.readAllMapAsync()
                        .thenApply(data -> {
                            if (data.isEmpty()) {
                                return null;
                            }

                            return new KardiaPlayer(uuid, playerData.get("name"), playerData.get("server"));
                        }).toCompletableFuture();

                futures.add(playerFuture);
            });

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> futures.stream()
                            .map(CompletableFuture::join)
                            .filter(Objects::nonNull)
                            .toList()
                    );
        }).toCompletableFuture();
    }

    public List<KardiaServer> cachedServers() {
        return cachedServers;
    }

    public ServerEnvironment environment() {
        return serverEnvironment;
    }

    public ServerType serverType() {
        return serverType;
    }

}
