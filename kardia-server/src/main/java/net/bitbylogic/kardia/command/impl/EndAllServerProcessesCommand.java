package net.bitbylogic.kardia.command.impl;

import net.bitbylogic.kardia.Kardia;
import net.bitbylogic.kardia.command.Command;
import net.bitbylogic.kardia.command.CommandSender;

public class EndAllServerProcessesCommand extends Command {

    public EndAllServerProcessesCommand() {
        super(
                "endallprocesses",
                "Stops all currently running servers.",
                "endallprocesses",
                null
        );
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("Stopping all containers.");
        Kardia.network().activeContainers().forEach(Kardia.network()::stopContainer);
    }
}
