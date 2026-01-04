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
            List.of("messages.zip", "minigames.zip")
    ),
    PROXY(
            "proxy",
            "Velocity.jar",
            List.of("velocity.toml"),
            "-Xms512m -Xmx1024m",
            null,
            List.of("plugins.zip", "forwarding.secret")
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

    public String prefix() {
        return prefix;
    }

    public String jarName() {
        return jarName;
    }

    public List<String> configFiles() {
        return configFiles;
    }

    public String commandLineArguments() {
        return commandLineArguments;
    }

    public String postCommandLineArguments() {
        return postCommandLineArguments;
    }

    public List<String> requiredConfigs() {
        return requiredConfigs;
    }

}
