package net.bitbylogic.kardia.command.impl;

import net.bitbylogic.kardia.Kardia;
import net.bitbylogic.kardia.command.Command;
import net.bitbylogic.kardia.command.CommandSender;

public class ServersCommand extends Command {

    public ServersCommand() {
        super("servers", "Lists all servers.", "servers", null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("----------------------------------------");
        sender.sendMessage("KARDIA ID - BOUND PORT - JOIN STATE - PLAYERS");
        Kardia.network().activeServers().forEach(server ->
            sender.sendMessage(server.kardiaId() + " - " + server.boundPort() + " - " +
                    server.joinState().name() + " - " + server.players().size())
        );
        sender.sendMessage("----------------------------------------");
    }
}
