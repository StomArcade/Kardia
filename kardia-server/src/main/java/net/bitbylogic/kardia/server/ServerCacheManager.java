package net.bitbylogic.kardia.server;

import net.bitbylogic.kardia.Kardia;
import net.bitbylogic.kardia.util.RedisKeys;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;

public class ServerCacheManager {

    private final RedissonClient redisson;

    public ServerCacheManager(RedissonClient redisson) {
        this.redisson = redisson;
    }

    public KardiaServer registerServer(String dockerId, String kardiaId, String instance, List<String> ids, String ip, int boundPort, ServerType type) {
        KardiaServer server = new KardiaServer(dockerId, kardiaId, instance, ids, type, ip, boundPort,
                500, KardiaServer.JoinState.NOT_JOINABLE, false, new ArrayList<>());

        redisson.getMap(RedisKeys.SERVERS).fastPut(kardiaId, Kardia.gson().toJson(server));

        return server;
    }

    public void updateServer(KardiaServer server) {
        RMap<String, String> servers = redisson.getMap(RedisKeys.SERVERS);

        servers.replace(server.kardiaId(), Kardia.gson().toJson(server));
    }

    public void unregisterServer(String kardiaId) {
        redisson.getMap(RedisKeys.SERVERS).fastRemove(kardiaId);
    }

    public void unregisterAll() {
        redisson.getMap(RedisKeys.SERVERS).delete();
    }

}
