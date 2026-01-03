package net.bitbylogic.kardia.command.impl;

import net.bitbylogic.kardia.Kardia;
import net.bitbylogic.kardia.command.Command;
import net.bitbylogic.kardia.command.CommandSender;

public class InvalidateRedisCommand extends Command {

    public InvalidateRedisCommand() {
        super("invalidateredis", "Invalidates Redis server cache.",
                "invalidateredis", null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Kardia.serverManager().unregisterAll();
        sender.sendMessage("Invalidated all servers from Redis!");
    }
}
