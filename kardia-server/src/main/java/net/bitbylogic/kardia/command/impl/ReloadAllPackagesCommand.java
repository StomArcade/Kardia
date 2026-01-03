package net.bitbylogic.kardia.command.impl;

import net.bitbylogic.kardia.Kardia;
import net.bitbylogic.kardia.command.Command;
import net.bitbylogic.kardia.command.CommandSender;

public class ReloadAllPackagesCommand extends Command {

    public ReloadAllPackagesCommand() {
        super(
                "reloadallpackages",
                "Reloads all packages.",
                "reloadallpackages <invalidate_current_cache> <build_images>",
                null
        );
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length >= 2) {
            try {
                boolean invalidate = Boolean.parseBoolean(args[0]);
                boolean build = Boolean.parseBoolean(args[1]);

                if(invalidate) {
                    Kardia.network().packageCache().invalidateAll();
                    sender.sendMessage("Invalidate the entirety of the package cache. " +
                            "Please note that this could cause games to not meet their quota for some time.");
                    sender.sendWarning("IMPORTANT: If image building is set to false, this " +
                            "will halt all server upscaling.");
                }

                Kardia.network().loadAllPackages();

                if(build) {
                    sender.sendMessage("Beginning image building (this can take some time).");
                    Kardia.network().packageCache().asMap().values()
                            .forEach(pkg -> Kardia.network().createImage(pkg));
                    sender.sendWarning("Processed all package image build. Please note that this can take " +
                            "some time and can cause server quota expectations to not be met.");
                }
            } catch (Exception e) {
                sender.sendError("Could not parse boolean!");
            }
        } else {
            sender.sendError("Please specify whether to invalidate the current cache and build images");
        }
    }
}
