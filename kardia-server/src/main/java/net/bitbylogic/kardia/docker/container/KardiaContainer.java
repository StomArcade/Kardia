package net.bitbylogic.kardia.docker.container;

public sealed class KardiaContainer permits GenericKardiaContainer {

    private final String dockerId;
    private final String kardiaId;
    private final int boundPort;

    public KardiaContainer(String dockerId, String kardiaId, int boundPort) {
        this.dockerId = dockerId;
        this.kardiaId = kardiaId;
        this.boundPort = boundPort;
    }

    public String dockerId() {
        return dockerId;
    }

    public String kardiaId() {
        return kardiaId;
    }

    public int boundPort() {
        return boundPort;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (KardiaContainer) obj;
        return java.util.Objects.equals(this.dockerId, that.dockerId) &&
                java.util.Objects.equals(this.kardiaId, that.kardiaId) &&
                this.boundPort == that.boundPort;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(dockerId, kardiaId, boundPort);
    }

    @Override
    public String toString() {
        return "KardiaContainer[" +
                "dockerId=" + dockerId + ", " +
                "kardiaId=" + kardiaId + ", " +
                "boundPort=" + boundPort + ']';
    }

}