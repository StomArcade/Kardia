package net.bitbylogic.kardia.command.impl;

import net.bitbylogic.kardia.Kardia;
import net.bitbylogic.kardia.command.Command;
import net.bitbylogic.kardia.command.CommandSender;

import java.io.IOException;

public class ReloadConfigCommand extends Command {

    public ReloadConfigCommand() {
        super("reloadconfig", "Reload the config.", "reloadconfig", null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            Kardia.config().load();
            sender.sendMessage("Successfully reloaded config!");
        } catch (IOException e) {
            Kardia.LOGGER.error("Failed to reload config!", e);
        }
    }

}
