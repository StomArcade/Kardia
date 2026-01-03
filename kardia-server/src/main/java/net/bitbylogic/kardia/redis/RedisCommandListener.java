package net.bitbylogic.kardia.redis;

import net.bitbylogic.kardia.Kardia;
import net.bitbylogic.kardia.command.CommandSender;
import net.bitbylogic.rps.listener.ListenerComponent;
import net.bitbylogic.rps.listener.RedisMessageListener;

import java.util.UUID;

public class RedisCommandListener extends RedisMessageListener {

    public RedisCommandListener() {
        super("kardia_command");
    }

    @Override
    public void onReceive(ListenerComponent component) {
        UUID playerId = component.getData("playerId", UUID.class);
        String command = component.getData("command", String.class);

        CommandSender sender = new CommandSender() {
            @Override
            public void sendMessage(String message) {
                Kardia.redisClient().sendListenerMessage(
                        new ListenerComponent(null, "player_message").addData("message", "<secondary>" + message).addData("playerId", playerId)
                );
            }

            @Override
            public void sendWarning(String warning) {
                Kardia.redisClient().sendListenerMessage(
                        new ListenerComponent(null, "player_message").addData("message", "<error_secondary>" + warning).addData("playerId", playerId)
                );
            }

            @Override
            public void sendError(String error) {
                Kardia.redisClient().sendListenerMessage(
                        new ListenerComponent(null, "player_message").addData("message", "<error_highlight>" + error).addData("playerId", playerId)
                );
            }
        };

        boolean executed = Kardia.commands().executeCommand(sender, command);

        if(executed) {
            return;
        }

        sender.sendError("That command doesn't exist. Use help for more information (/network help).");
    }
}
