package net.bitbylogic.kardiavelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.bitbylogic.kardiavelocity.auth.KardiaAuth;
import net.bitbylogic.kardiavelocity.command.KardiaCommand;
import net.bitbylogic.kardiavelocity.command.LobbyCommand;
import net.bitbylogic.kardiavelocity.command.WhereAmICommand;
import net.bitbylogic.kardiavelocity.listener.ConnectionListener;
import net.bitbylogic.kardiavelocity.listener.ServerKickListener;
import net.bitbylogic.kardiavelocity.message.manager.MessageManager;
import net.bitbylogic.kardiavelocity.message.messages.BrandingMessages;
import net.bitbylogic.kardiavelocity.redis.ConnectListener;
import net.bitbylogic.kardiavelocity.redis.DisconnectListener;
import net.bitbylogic.kardiavelocity.server.ServerManager;
import net.bitbylogic.rps.RedisManager;
import net.bitbylogic.rps.client.RedisClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;

@Plugin(
        id = "kardiavelocity",
        name = "KardiaVelocity",
        version = "1.0.0",
        authors = {"BitByLogic"}
)
public class KardiaVelocity {

    private static KardiaVelocity instance;

    private final ProxyServer proxyServer;
    private final Logger logger;

    private RedisManager redisManager;
    private RedisClient redisClient;
    private ServerManager serverManager;
    private MessageManager messageManager;

    @Inject
    public KardiaVelocity(ProxyServer proxyServer, Logger logger) {
        instance = this;
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        String host = System.getenv("REDIS_HOST");
        if (host == null || host.isBlank()) {
            logger.error("REDIS_HOST not set — shutting down proxy");
            proxyServer.shutdown();
            return;
        }

        int port = 6379;
        try {
            String portEnv = System.getenv("REDIS_PORT");
            if (portEnv != null) {
                port = Integer.parseInt(portEnv);
            }
        } catch (NumberFormatException ignored) {}

        String password = System.getenv("REDIS_PASSWORD");
        String sourceId = System.getenv("REDIS_SOURCE_ID");

        if (sourceId == null || sourceId.isBlank()) {
            logger.error("REDIS_SOURCE_ID not set — shutting down proxy");
            proxyServer.shutdown();
            return;
        }

        this.redisManager = new RedisManager(host, port, password, sourceId, new Config().setCodec(StringCodec.INSTANCE));
        this.redisClient = redisManager.registerClient(sourceId);

        redisClient.registerListener(new ConnectListener());
        redisClient.registerListener(new DisconnectListener());

        this.serverManager = new ServerManager(this, proxyServer);
        serverManager.start();

        this.messageManager = new MessageManager();
        messageManager.registerGroup(new BrandingMessages());

        KardiaAuth.invalidateAll();

        proxyServer.getEventManager().register(this, new ConnectionListener());
        proxyServer.getEventManager().register(this, new ServerKickListener());

        proxyServer.getCommandManager().register("kardia", new KardiaCommand());
        proxyServer.getCommandManager().register("lobby", new LobbyCommand());
        proxyServer.getCommandManager().register("whereami", new WhereAmICommand());

        logger.info("KardiaVelocity successfully initialized.");
    }

    public static KardiaVelocity getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public RedisClient getRedisClient() {
        return redisClient;
    }

    public ServerManager getServerManager() {
        return serverManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
}