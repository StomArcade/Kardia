package net.bitbylogic.kardia.server;

import java.util.ArrayList;
import java.util.List;

public enum ServerType {

    LOBBY(
            "lobby",
            "StomArcade.jar",
            new ArrayList<>(),
            "-Xms512m -Xmx1024m -Dcom.mojang.eula.agree=true",
            "",
            List.of("messages.zip")
    ),
    GAME(
            "game",
            "StomArcade.jar",
            new ArrayList<>(),
            "-Xms512m -Xmx1024m -Dcom.mojang.eula.agree=true",
            "",
            List.of("messages.zip")
    ),
    PROXY(
            "proxy",
            "Velocity.jar",
            List.of("config.yml"),
            "-Xms512m -Xmx1024m",
            null,
            List.of("KardiaVelocity")
    );

    private final String prefix;
    private final String jarName;
    private final List<String> configFiles;
    private final String commandLineArguments;
    private final String postCommandLineArguments;
    private final List<String> requiredConfigs;

    ServerType(String prefix, String jarName, List<String> configFiles, String commandLineArguments, String postCommandLineArguments, List<String> requiredConfigs) {
        this.prefix = prefix;
        this.jarName = jarName;
        this.configFiles = configFiles;
        this.commandLineArguments = commandLineArguments;
        this.postCommandLineArguments = postCommandLineArguments;
        this.requiredConfigs = requiredConfigs;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getJarName() {
        return jarName;
    }

    public List<String> getConfigFiles() {
        return configFiles;
    }

    public String getCommandLineArguments() {
        return commandLineArguments;
    }

    public String getPostCommandLineArguments() {
        return postCommandLineArguments;
    }

    public List<String> getRequiredConfigs() {
        return requiredConfigs;
    }

}
