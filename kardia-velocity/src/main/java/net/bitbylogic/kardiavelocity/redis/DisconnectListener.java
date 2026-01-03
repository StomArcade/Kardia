package net.bitbylogic.kardiavelocity.redis;

import com.velocitypowered.api.proxy.Player;
import net.bitbylogic.kardiavelocity.KardiaVelocity;
import net.bitbylogic.rps.listener.ListenerComponent;
import net.bitbylogic.rps.listener.RedisMessageListener;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class DisconnectListener extends RedisMessageListener {

    public static String CHANNEL = "disconnect";

    public DisconnectListener() {
        super(CHANNEL);
    }

    @Override
    public void onReceive(ListenerComponent component) {
        UUID playerId = component.getData("player", UUID.class);
        String message = component.getData("message", String.class);

        Player player = KardiaVelocity.getInstance().getProxyServer().getPlayer(playerId).orElse(null);

        if (player == null) {
            return;
        }

        if(message != null) {
            player.disconnect(Component.text(message));
        } else {
            player.disconnect(Component.empty());
        }
    }
}
