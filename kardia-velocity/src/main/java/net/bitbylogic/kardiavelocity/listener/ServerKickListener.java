package net.bitbylogic.kardiavelocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.bitbylogic.kardia.server.KardiaServer;
import net.bitbylogic.kardiavelocity.KardiaVelocity;
import net.bitbylogic.kardiavelocity.util.message.MessageUtil;
import net.kyori.adventure.text.Component;

public class ServerKickListener {

    @Subscribe
    public void onServerKick(KickedFromServerEvent e) {
        ServerInfo server = e.getServer().getServerInfo();

        if(!server.getName().equalsIgnoreCase("fallback")) {
            KardiaServer kardiaServer = KardiaVelocity.getInstance().getServerManager().getPriorityServerById("arcade_lounge");
            if(kardiaServer != null) {
                ServerInfo asInfo = KardiaVelocity.getInstance().getServerManager().constructServerInfo(kardiaServer);
                if(!asInfo.getAddress().getHostName().equals(server.getAddress().getHostName()) ||
                        asInfo.getAddress().getPort() != server.getAddress().getPort()) {
                    e.setResult(KickedFromServerEvent.RedirectPlayer.create(KardiaVelocity.getInstance().getProxyServer().getServer(asInfo.getName()).get()));
                }
            }

            e.getServerKickReason().ifPresent(component -> e.getPlayer().sendMessage(component));
            e.setResult(KickedFromServerEvent.Notify.create(MessageUtil.error("Unable to connect to server.")));
        }
    }
}
