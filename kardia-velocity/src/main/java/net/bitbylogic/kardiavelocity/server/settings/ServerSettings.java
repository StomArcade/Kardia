package net.bitbylogic.kardiavelocity.server.settings;

import net.bitbylogic.kardia.server.KardiaServer;

public class ServerSettings {

    private boolean privateServer;
    private boolean disableStatTracking;
    private boolean offlineMode;

    private int maxPlayers;
    private KardiaServer.JoinState joinState;
    private String motd;

    public ServerSettings() {
        this.privateServer = false;
        this.disableStatTracking = false;
        this.offlineMode = false;

        this.maxPlayers = 500;
        this.joinState = KardiaServer.JoinState.NOT_JOINABLE;
        this.motd = null;
    }

    public boolean isPrivateServer() {
        return privateServer;
    }

    public boolean isDisableStatTracking() {
        return disableStatTracking;
    }

    public boolean isOfflineMode() {
        return offlineMode;
    }

    public int maxPlayers() {
        return maxPlayers;
    }

    public KardiaServer.JoinState joinState() {
        return joinState;
    }

    public String motd() {
        return motd;
    }

    public void setJoinState(KardiaServer.JoinState joinState) {
        this.joinState = joinState;
    }

}
