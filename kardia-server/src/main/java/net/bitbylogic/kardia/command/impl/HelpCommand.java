package net.bitbylogic.kardia.command.impl;

import net.bitbylogic.kardia.Kardia;
import net.bitbylogic.kardia.command.Command;
import net.bitbylogic.kardia.command.CommandSender;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "Get help information.", "help", null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("----------------------------------------");
        Kardia.commands().commands().forEach(command ->
            sender.sendMessage(command.name() + " - " + command.description()));
        sender.sendMessage("----------------------------------------");
    }
}
