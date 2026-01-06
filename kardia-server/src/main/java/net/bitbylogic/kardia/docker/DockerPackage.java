package net.bitbylogic.kardia.docker;

import net.bitbylogic.kardia.server.KardiaServer;
import net.bitbylogic.kardia.server.ServerType;

import java.util.List;
import java.util.Objects;

public final class DockerPackage {

    private final List<String> ids;
    private final String prefix;
    private final int cache;
    private final int portRangeMin;
    private final int portRangeMax;
    private final String root;
    private final List<String> envVars;
    private final ServerType serverType;

    private final List<String> includedFiles;
    private final String world;
    private final boolean privateServer;

    private final String sqlHost;
    private final int sqlPort;
    private final String sqlDatabase;
    private final String sqlUsername;
    private final String sqlPassword;

    private final String redisHost;
    private final int redisPort;
    private final String redisPassword;

    private transient String instance;

    public DockerPackage(List<String> ids, String prefix, int cache, int portRangeMin, int portRangeMax, String root,
                         List<String> envVars, ServerType serverType, List<String> includedFiles,
                         String world, boolean privateServer, String sqlHost, int sqlPort, String sqlDatabase,
                         String sqlUsername, String sqlPassword, String redisHost, int redisPort, String redisPassword) {
        this.ids = ids;
        this.prefix = prefix;
        this.cache = cache;
        this.portRangeMin = portRangeMin;
        this.portRangeMax = portRangeMax;
        this.root = root;
        this.envVars = envVars;
        this.serverType = serverType;
        this.includedFiles = includedFiles;
        this.world = world;
        this.privateServer = privateServer;
        this.sqlHost = sqlHost;
        this.sqlPort = sqlPort;
        this.sqlDatabase = sqlDatabase;
        this.sqlUsername = sqlUsername;
        this.sqlPassword = sqlPassword;
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.redisPassword = redisPassword;
    }

    public String root() {
        return root;
    }

    public List<String> ids() {
        return ids;
    }

    public String prefix() {return prefix;}

    public int cache() {
        return cache;
    }

    public int portMin() {
        return portRangeMin;
    }

    public int portMax() {
        return portRangeMax;
    }

    public List<String> envVars() {
        return envVars;
    }

    public ServerType serverType() {
        return serverType;
    }

    public List<String> includedFiles() {
        return includedFiles;
    }

    public String world() {
        return world;
    }

    public String instance() {
        return instance;
    }

    public boolean isPrivateServer() {
        return privateServer;
    }

    public String sqlHost() {
        return sqlHost;
    }

    public int sqlPort() {
        return sqlPort;
    }

    public String sqlDatabase() {
        return sqlDatabase;
    }

    public String sqlUsername() {
        return sqlUsername;
    }

    public String sqlPassword() {
        return sqlPassword;
    }

    public String redisHost() {
        return redisHost;
    }

    public int redisPort() {
        return redisPort;
    }

    public String redisPassword() {
        return redisPassword;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public boolean isPortRangeValid() {
        return !(portRangeMin == -1 || portRangeMax == -1);
    }

    public String getImageKey() {
        return KardiaServer.PREFIX + instance + "_" + ids.getFirst();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DockerPackage that = (DockerPackage) o;
        return cache == that.cache && portRangeMin == that.portRangeMin && portRangeMax == that.portRangeMax &&
                sqlPort == that.sqlPort && redisPort == that.redisPort && privateServer == that.privateServer &&
                Objects.equals(ids, that.ids) && Objects.equals(prefix, that.prefix) && Objects.equals(root, that.root) && Objects.equals(envVars, that.envVars)
                && serverType == that.serverType && Objects.equals(includedFiles, that.includedFiles)
                && Objects.equals(world, that.world) && Objects.equals(instance, that.instance) &&
                Objects.equals(sqlHost, that.sqlHost) && Objects.equals(sqlDatabase, that.sqlDatabase) &&
                Objects.equals(sqlUsername, that.sqlUsername) && Objects.equals(sqlPassword, that.sqlPassword) &&
                Objects.equals(redisHost, that.redisHost) && Objects.equals(redisPassword, that.redisPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ids, prefix, cache, portRangeMin, portRangeMax, root, envVars, serverType, includedFiles, world, instance,
                privateServer, sqlHost, sqlPort, sqlDatabase, sqlUsername, sqlPassword, redisHost, redisPort, redisPassword);
    }

    @Override
    public String toString() {
        return "DockerPackage{" +
                "ids=" + ids +
                ", prefix='" + prefix + '\'' +
                ", cache=" + cache +
                ", portRangeMin=" + portRangeMin +
                ", portRangeMax=" + portRangeMax +
                ", root='" + root + '\'' +
                ", envVars=" + envVars +
                ", serverType=" + serverType +
                ", includedPlugins=" + includedFiles +
                ", world='" + world + '\'' +
                ", instance='" + instance + '\'' +
                ", privateServer=" + privateServer +
                ", sqlHost='" + sqlHost + '\'' +
                ", sqlPort=" + sqlPort +
                ", sqlDatabase='" + sqlDatabase + '\'' +
                ", sqlUsername='" + sqlUsername + '\'' +
                ", sqlPassword='" + sqlPassword + '\'' +
                ", redisHost='" + redisHost + '\'' +
                ", redisPort=" + redisPort +
                ", redisPassword='" + redisPassword + '\'' +
                '}';
    }

}