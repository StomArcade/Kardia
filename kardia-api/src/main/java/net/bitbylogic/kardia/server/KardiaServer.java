package net.bitbylogic.kardia.server;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record KardiaServer(String dockerId, String kardiaId, String instance, List<String> ids, ServerType serverType, String ip,
                           int boundPort, int maxPlayers, JoinState joinState, boolean privateServer, List<UUID> players) {

    public static String PREFIX = "kardia_";

    public enum JoinState {
        JOINABLE,
        NOT_JOINABLE;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        KardiaServer that = (KardiaServer) o;
        return boundPort == that.boundPort && maxPlayers == that.maxPlayers && privateServer == that.privateServer
                && Objects.equals(ip, that.ip) && Objects.equals(dockerId, that.dockerId) && Objects.equals(kardiaId, that.kardiaId) && Objects.equals(instance, that.instance)
                && Objects.equals(ids, that.ids) && Objects.equals(players, that.players) && joinState == that.joinState && serverType == that.serverType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dockerId, kardiaId, instance, ids, serverType, ip, boundPort, maxPlayers, joinState, privateServer, players);
    }

    @Override
    public String toString() {
        return "KardiaServer{" +
                "dockerId='" + dockerId + '\'' +
                ", kardiaId='" + kardiaId + '\'' +
                ", instance='" + instance + '\'' +
                ", ids=" + ids +
                ", serverType=" + serverType +
                ", ip='" + ip + '\'' +
                ", boundPort=" + boundPort +
                ", maxPlayers=" + maxPlayers +
                ", joinState=" + joinState +
                ", privateServer=" + privateServer +
                ", players=" + players +
                '}';
    }

}
