package net.bitbylogic.kardiavelocity.redis;

import com.velocitypowered.api.proxy.ProxyServer;
import net.bitbylogic.kardiavelocity.KardiaVelocity;
import net.bitbylogic.rps.listener.ListenerComponent;
import net.bitbylogic.rps.listener.RedisMessageListener;

public class CommandListener extends RedisMessageListener {

    public CommandListener() {
        super("server_command");
    }

    @Override
    public void onReceive(ListenerComponent component) {
        String command = component.getData("command", String.class);

        ProxyServer proxyServer = KardiaVelocity.getInstance().getProxyServer();
        proxyServer.getCommandManager().executeImmediatelyAsync(proxyServer.getConsoleCommandSource(), command);
    }

}
