package net.bitbylogic.kardia.redis;

import net.bitbylogic.kardia.Kardia;
import net.bitbylogic.kardia.server.KardiaServer;
import net.bitbylogic.rps.listener.ListenerComponent;
import net.bitbylogic.rps.listener.RedisMessageListener;

public class ServerShutdownListener extends RedisMessageListener {

    public ServerShutdownListener() {
        super("server_shutdown");
    }

    @Override
    public void onReceive(ListenerComponent component) {
        String serverId = component.getData("serverId", String.class);

        KardiaServer server = Kardia.network().getServerByKardiaID(serverId);

        if (server == null) {
            return;
        }

        Kardia.network().stopServer(server);
    }

}
