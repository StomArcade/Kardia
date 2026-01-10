package net.bitbylogic.kardiavelocity.listener;

import com.google.common.collect.Lists;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import net.bitbylogic.kardia.server.KardiaServer;
import net.bitbylogic.kardia.server.ServerType;
import net.bitbylogic.kardiavelocity.KardiaVelocity;
import net.bitbylogic.kardiavelocity.auth.KardiaAuth;
import net.bitbylogic.kardiavelocity.server.player.KardiaPlayer;
import net.bitbylogic.kardiavelocity.util.message.MessageUtil;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.UUID;

public class ConnectionListener {

    private List<UUID> logins;

    public ConnectionListener() {
        this.logins = Lists.newCopyOnWriteArrayList();
    }

    @Subscribe
    public void onLogin(LoginEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        KardiaAuth.AuthResult result = KardiaAuth.auth(uuid);

        String message = null;

        switch (result) {
            case AUTHENTICATED:
                break;
            case AUTHENTICATION_EXISTS:
                message = "Could not authenticate! Another player on your account?";
                break;
            case NOT_AUTHENTICATED:
                message = "Could not authenticate!";
                break;
            case NO_CONNECTION:
                message = "Network is currently down!";
                break;
            default:
                message = "An unknown error occurred while authenticating.";
                break;
        }

        if(result != KardiaAuth.AuthResult.AUTHENTICATED) {
            e.getPlayer().disconnect(Component.text(message));
        } else {
            KardiaVelocity.getInstance().getServerManager().addPlayer(new KardiaPlayer(
                    e.getPlayer().getUniqueId(),
                    e.getPlayer().getUsername()
            ));
        }
    }

    @Subscribe(priority = 10)
    public void onProxyConnect(PlayerChooseInitialServerEvent e) {
        if (!this.logins.contains(e.getPlayer().getUniqueId())) {
            KardiaServer server = KardiaVelocity.getInstance()
                    .getServerManager()
                    .getPriorityServerById(KardiaVelocity.getInstance().getServerManager().environment().getEnv("LOBBY_ID", "fallback"));

            if (server != null && server.joinState() == KardiaServer.JoinState.JOINABLE && !server.privateServer()) {
                e.setInitialServer(KardiaVelocity.getInstance()
                        .getProxyServer()
                        .getServer(server.kardiaId())
                        .orElseThrow());
                this.logins.add(e.getPlayer().getUniqueId());
            } else {
                e.setInitialServer(KardiaVelocity.getInstance().getProxyServer().getServer("fallback").orElseThrow());
            }
        }
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent e) {
        KardiaVelocity.getInstance().getServerManager().updatePlayer(new KardiaPlayer(
                e.getPlayer().getUniqueId(),
                e.getPlayer().getUsername(),
                e.getServer().getServerInfo().getName()
        ));

        KardiaServer kardiaServer = KardiaVelocity.getInstance().getServerManager().getServerByKardiaId(e.getServer().getServerInfo().getName());

        if (kardiaServer == null || kardiaServer.serverType() != ServerType.PAPER_GAME) {
            return;
        }

        e.getPlayer().sendMessage(MessageUtil.error("This server is NOT running Minestom. All gameplay elements are created using the Spigot/Paper API."));
    }

    @Subscribe
    public void onProxyDisconnect(DisconnectEvent event) {
        KardiaAuth.invalidate(event.getPlayer().getUniqueId());
        this.logins.remove(event.getPlayer().getUniqueId());

        ServerConnection serverConnection = event.getPlayer().getCurrentServer().orElse(null);

        KardiaVelocity.getInstance().getServerManager().removePlayer(new KardiaPlayer(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getUsername(),
                serverConnection == null ? "none" : serverConnection.getServerInfo().getName()
        ));
    }
}
