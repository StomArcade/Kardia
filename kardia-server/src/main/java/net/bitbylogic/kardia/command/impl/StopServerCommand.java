package net.bitbylogic.kardia.command.impl;

import net.bitbylogic.kardia.Kardia;
import net.bitbylogic.kardia.command.Command;
import net.bitbylogic.kardia.command.CommandSender;
import net.bitbylogic.kardia.server.KardiaServer;

public class StopServerCommand extends Command {

    public StopServerCommand() {
        super("stopserver", "Stops a server by Kardia ID.", "stopserver <id>", null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length >= 1) {
            KardiaServer server = Kardia.network().getServerByKardiaID(args[0]);
            if(server != null) {
                sender.sendMessage("Sending request to stop server " + server.kardiaId() + ". " +
                        "You will be notified when it is stopped.");
                sender.sendWarning("If you do not receive a message within a minute, " +
                        "the server most likely stopped successfully.");
                Kardia.network().stopServer(server).thenAccept(completed -> {
                    if(completed) {
                        sender.sendMessage("Successfully stopped server " + server.kardiaId() + "!");
                    } else {
                        sender.sendError("Could not stop server " + server.kardiaId() + "! " +
                                "Perhaps it is already stopped?");
                    }
                });
            } else {
                sender.sendError("That server doesn't exist!");
            }
        } else {
            sender.sendError("Please specify the Kardia ID of the server you wish to stop.");
        }
    }
}
