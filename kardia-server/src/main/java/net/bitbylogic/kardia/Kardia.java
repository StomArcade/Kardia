package net.bitbylogic.kardia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.bitbylogic.kardia.command.CommandManager;
import net.bitbylogic.kardia.docker.NetworkManager;
import net.bitbylogic.kardia.redis.RedisCommandListener;
import net.bitbylogic.kardia.redis.ServerShutdownListener;
import net.bitbylogic.kardia.server.ServerCacheManager;
import net.bitbylogic.rps.RedisManager;
import net.bitbylogic.rps.client.RedisClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class Kardia {

    private static final Gson GSON = new Gson();
    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final File CONFIG_FILE = new File("config.yml");
    public static final Logger LOGGER = LoggerFactory.getLogger("Kardia");

    private static YamlFile config;

    private static RedisManager redisManager;
    private static RedisClient redisClient;

    private static NetworkManager networkManager;
    private static ServerCacheManager serverCacheManager;
    private static CommandManager commandManager;

    public static void main(String[] args) {
        loadConfig();
        loadRedis();

        networkManager = new NetworkManager();
        serverCacheManager = new ServerCacheManager(redisClient.getRedisClient());
        commandManager = new CommandManager();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down...");

            commandManager.commandService().shutdown();
            networkManager.shutdown();
            serverCacheManager.unregisterAll();
            redisManager.getRedissonClient().shutdown();
        }));
    }

    private static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            try (InputStream inputStream = Kardia.class.getClassLoader().getResourceAsStream("config.yml")) {
                if(inputStream == null) {
                    LOGGER.error("Failed to load config!");
                    System.exit(1);
                    return;
                }

                Files.write(CONFIG_FILE.toPath(), inputStream.readAllBytes());

                config = YamlFile.loadConfiguration(CONFIG_FILE);
            } catch (IOException e) {
                LOGGER.error("Failed to load config!", e);
            }

            return;
        }

        try {
            config = YamlFile.loadConfiguration(CONFIG_FILE);
        } catch (IOException e) {
            LOGGER.error("Failed to load config!", e);
        }
    }

    private static void loadRedis() {
        ConfigurationSection redisSection = config.getConfigurationSection("Redis");

        if(redisSection == null) {
            LOGGER.error("Redis section not found in config.yml!");
            System.exit(1);
            return;
        }

        String host = redisSection.getString("Host");
        int port = redisSection.getInt("Port");
        String password = redisSection.getString("Password");
        String identifier = redisSection.getString("Identifier");

        redisManager = new RedisManager(host, port, password, identifier, new Config().setCodec(StringCodec.INSTANCE));
        redisClient = redisManager.registerClient(identifier);

        redisClient.registerListener(new RedisCommandListener());
        redisClient.registerListener(new ServerShutdownListener());
    }

    public static Gson gson() {
        return GSON;
    }

    public static Gson prettyGson() {
        return PRETTY_GSON;
    }

    public static YamlFile config() {
        return config;
    }

    public static RedisManager redis() {
        return redisManager;
    }

    public static RedisClient redisClient() {
        return redisClient;
    }

    public static NetworkManager network() {
        return networkManager;
    }

    public static ServerCacheManager serverManager() {
        return serverCacheManager;
    }

    public static CommandManager commands() {
        return commandManager;
    }

}
