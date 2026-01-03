package net.bitbylogic.kardiavelocity.redis;

import com.velocitypowered.api.proxy.Player;
import net.bitbylogic.kardia.server.KardiaServer;
import net.bitbylogic.kardiavelocity.KardiaVelocity;
import net.bitbylogic.rps.listener.ListenerComponent;
import net.bitbylogic.rps.listener.RedisMessageListener;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class ConnectListener extends RedisMessageListener {

    public ConnectListener() {
        super("connect");
    }

    @Override
    public void onReceive(ListenerComponent component) {
        // Form: uuid <id|server> <code>
        UUID playerId = component.getData("player", UUID.class);
        ConnectType connectType = component.getData("server", ConnectType.class);
        String id = component.getData("id", String.class);

        if(connectType != null) {
            Player player = KardiaVelocity.getInstance().getProxyServer().getPlayer(playerId).orElse(null);

            if(player != null) {
                KardiaServer server = connectType == ConnectType.ID ?
                        KardiaVelocity.getInstance().getServerManager().getPriorityServerById(id) :
                        KardiaVelocity.getInstance().getServerManager().getPriorityServerByInstance(id);
                if(server != null) {
                    KardiaVelocity.getInstance().getServerManager().connectPlayer(player, server, false);
                } else {
                    player.sendMessage(Component.text("§cCould not find a server with id " +
                            id + "!"));
                }
            }
        }
    }

    public enum ConnectType {
        ID,
        SERVER
    }

}
