package net.bitbylogic.kardia.command.impl;

import net.bitbylogic.kardia.Kardia;
import net.bitbylogic.kardia.command.Command;
import net.bitbylogic.kardia.command.CommandSender;
import net.bitbylogic.kardia.docker.container.KardiaContainer;

public class StopContainerCommand extends Command {

    public StopContainerCommand() {
        super(
                "stopcontainer",
                "Stops a container by container Id.",
                "stopcontainer <string:container_id>",
                null
        );
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length >= 1) {
            KardiaContainer container = Kardia.network().getContainerByDockerId(args[0]);
            if(container != null) {
                sender.sendMessage("Sending request to stop container " + container.dockerId() + ". " +
                        "You will be notified when it is stopped.");
                Kardia.network().stopContainer(container).thenAccept(completed -> {
                    if(completed) {
                        sender.sendMessage("Successfully stopped container " + args[0] + "!");
                    } else {
                        sender.sendError("Could not stop container " + args[0] + "! Perhaps it is already stopped?");
                    }
                });
            } else {
                sender.sendError("That container doesn't exist!");
            }
        } else {
            sender.sendError("Please specify a container Id.");
        }
    }
}
