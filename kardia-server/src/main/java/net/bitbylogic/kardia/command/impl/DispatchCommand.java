package net.bitbylogic.kardia.command.impl;

import net.bitbylogic.kardia.Kardia;
import net.bitbylogic.kardia.command.Command;
import net.bitbylogic.kardia.command.CommandSender;
import net.bitbylogic.rps.listener.ListenerComponent;
import org.apache.commons.lang3.StringUtils;

public class DispatchCommand extends Command {

    public DispatchCommand() {
        super("dispatch", "Dispatch a command to a server, do not include /.", "dispatch <name> <command>", null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 2) {
            sender.sendError("§cInvalid Arguments, Usage: " + usage());
            return;
        }

        String server = args[0];
        String command = StringUtils.join(args, " ", 1, args.length);

        sender.sendMessage("Dispatching command '" + command + "' to '" + server + "'!");
        Kardia.redisClient().sendListenerMessage(new ListenerComponent(server, "server_command").addData("command", command));
    }

}
