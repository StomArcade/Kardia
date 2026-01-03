package net.bitbylogic.kardia.command.impl;

import net.bitbylogic.kardia.Kardia;
import net.bitbylogic.kardia.command.Command;
import net.bitbylogic.kardia.command.CommandSender;

public class ListContainerCommand extends Command {

    public ListContainerCommand() {
        super("listcontainer", "Lists all containers active.", "listcontainer", null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("----------------------------------------");
        sender.sendMessage("KARDIA ID - BOUND PORT");
        Kardia.network().activeContainers().forEach(container ->
            sender.sendMessage(container.kardiaId() + " - " + container.boundPort())
        );
        sender.sendMessage("----------------------------------------");
    }

}
