package net.bitbylogic.kardiavelocity.server;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerEnvironment {

    private final Map<String, String> envVariables = new ConcurrentHashMap<>();

    public ServerEnvironment() {
        setup();
    }

    private void setup() {
        for (EnvVariable type : EnvVariable.values()) {
            String variable = System.getenv(type.name());
            envVariables.put(type.name(), variable != null ? variable : type.getDefaultValue());
        }
    }

    public String getEnv(String var, String defaultVal) {
        String val = envVariables.get(var);

        if(val == null) {
            String systemVar = System.getenv(var);
            setEnv(var, systemVar == null ? defaultVal : systemVar);
        }

        return val != null ? val : defaultVal;
    }

    public String getEnv(EnvVariable type) {
        return getEnv(type.name(), "none");
    }

    public void setEnv(String var, String defaultVal) {
        String val = System.getenv(var);
        envVariables.put(var, val != null ? val : defaultVal);
    }

    public enum EnvVariable {

        KARDIA_ID("none"),
        KARDIA_IP("none"),
        KARDIA_BOUND_PORT("25565"),
        KARDIA_INSTANCE_NAME("n/a"),
        KARDIA_PRIVATE_SERVER("false");

        final String defaultValue;

        EnvVariable(@NotNull String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

    }
}
