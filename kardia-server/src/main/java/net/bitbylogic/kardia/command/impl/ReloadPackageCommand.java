package net.bitbylogic.kardia.command.impl;

import net.bitbylogic.kardia.Kardia;
import net.bitbylogic.kardia.command.Command;
import net.bitbylogic.kardia.command.CommandSender;
import net.bitbylogic.kardia.docker.DockerPackage;

public class ReloadPackageCommand extends Command {

    public ReloadPackageCommand() {
        super(
                "reloadpackage",
                "Reloads a package.",
                "reloadpackage <string:name> <boolean:build_images>",
                null
        );
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length >= 2) {
            try {
                boolean buildImages = Boolean.parseBoolean(args[1]);

                sender.sendMessage("Invalidating current package data for " + args[0] + ".");
                Kardia.network().packageCache().invalidate(args[0]);

                sender.sendMessage("Loading package data.");
                Kardia.network().loadPackage(args[0]);

                if(buildImages) {
                    sender.sendMessage("Retrieving all package data to build images.");
                    DockerPackage dockerPackage = Kardia.network().getPackage(args[0]);

                    if(dockerPackage != null) {
                        sender.sendMessage("Stopping all running containers for package " + args[0] + ".");
                        sender.sendMessage("Building image for package " + args[0] + ".");

                        Kardia.network().createImage(dockerPackage).thenAccept(_ -> {
                            sender.sendMessage("Finished building images.");
                        });
                    } else {
                        sender.sendWarning("Seems like the package is invalid.");
                    }
                }
            } catch (Exception e) {
                sender.sendError(e.getMessage());
            }
        } else {
            sender.sendError("Please specify the name of the package and whether to build images and stop running containers.");
        }
    }
}
