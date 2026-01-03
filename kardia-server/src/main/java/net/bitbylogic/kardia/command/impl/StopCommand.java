package net.bitbylogic.kardia.command.impl;

import net.bitbylogic.kardia.command.Command;
import net.bitbylogic.kardia.command.CommandSender;

public class StopCommand extends Command {

    public StopCommand() {
        super("stop", "Stop all running processes and the entirety of the network.",
                "stop", null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        System.exit(0);
    }

}
