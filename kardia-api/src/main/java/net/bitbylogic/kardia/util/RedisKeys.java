package net.bitbylogic.kardia.util;

public class RedisKeys {

    public static String SERVERS = "kardia:servers";

    public static final String PLAYERS = "kardia:players:%s";
    public static final String PLAYERS_BY_NAME = "kardia:players:name";

    public static final String AUTH = "kardia:player:auth";

    // Prevent initialization
    private RedisKeys() {

    }

}
