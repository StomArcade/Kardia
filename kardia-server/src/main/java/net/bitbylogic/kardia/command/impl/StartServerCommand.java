package net.bitbylogic.kardia.command.impl;

import net.bitbylogic.kardia.Kardia;
import net.bitbylogic.kardia.command.Command;
import net.bitbylogic.kardia.command.CommandSender;
import net.bitbylogic.kardia.docker.DockerPackage;
import net.bitbylogic.kardia.util.Callback;

public class StartServerCommand extends Command {

    public StartServerCommand() {
        super("startserver", "Starts a server by id.", "startserver <id>", null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length >= 1) {
            DockerPackage pkg = Kardia.network().getPackage(args[0]);
            if(pkg != null) {
                sender.sendMessage("Starting package...");
                Kardia.network().startPackage(pkg, new Callback<String>() {
                    @Override
                    public void info(String s) {
                        sender.sendMessage(s);
                    }

                    @Override
                    public void error(String s) {
                        sender.sendError(s);
                    }
                });
            } else {
                sender.sendError("Invalid package id.");
            }
        } else {
            sender.sendError("Please specify a package id.");
        }
    }
}
