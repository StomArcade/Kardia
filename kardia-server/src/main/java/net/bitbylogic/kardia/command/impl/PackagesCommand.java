package net.bitbylogic.kardia.command.impl;

import net.bitbylogic.kardia.Kardia;
import net.bitbylogic.kardia.command.Command;
import net.bitbylogic.kardia.command.CommandSender;

import java.util.Arrays;

public class PackagesCommand extends Command {

    public PackagesCommand() {
        super("packages", "Lists all available packages.", "packages", null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("----------------------------------------");
        sender.sendMessage("Package -> Image IDs");
        Kardia.network().packageCache().asMap().forEach((instance, dockerPackage) ->
                sender.sendMessage(instance + " -> " + Arrays.toString(dockerPackage.ids().toArray())));
        sender.sendMessage("----------------------------------------");
    }
}
