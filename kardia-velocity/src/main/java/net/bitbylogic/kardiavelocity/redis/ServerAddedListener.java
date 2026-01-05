package net.bitbylogic.kardiavelocity.redis;

import net.bitbylogic.kardia.server.KardiaServer;
import net.bitbylogic.kardia.server.ServerType;
import net.bitbylogic.kardiavelocity.KardiaVelocity;
import net.bitbylogic.kardiavelocity.server.ServerManager;
import net.bitbylogic.rps.listener.ListenerComponent;
import net.bitbylogic.rps.listener.RedisMessageListener;

public class ServerAddedListener extends RedisMessageListener {

    public ServerAddedListener() {
        super("server_added");
    }

    @Override
    public void onReceive(ListenerComponent component) {
        KardiaServer server = component.getData("server", KardiaServer.class);
        ServerManager serverManager = KardiaVelocity.getInstance().getServerManager();

        if(server == null || server.serverType() == ServerType.PROXY || serverManager.cachedServers().contains(server)) {
            return;
        }

        serverManager.cachedServers().add(server);
        serverManager.rebuildProxyServerCache();
    }

}
