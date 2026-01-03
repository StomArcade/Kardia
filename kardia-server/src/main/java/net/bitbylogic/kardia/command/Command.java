package net.bitbylogic.kardia.command;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

public abstract class Command {

    private final String name;
    private final String description;
    private final String usage;
    private final List<String> aliases;

    public Command(String name, String description, String usage, List<String> aliases) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.aliases = (aliases == null ? Lists.newArrayList() : aliases);
    }

    public final boolean isAliasExist(String alias) {
        for (String anAlias : aliases) {
            if(!anAlias.equalsIgnoreCase(alias)) {
                continue;
            }

            return true;
        }

        return false;
    }

    public abstract void execute(CommandSender sender, String[] args);

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String usage() {
        return usage;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Command command = (Command) o;
        return Objects.equals(name, command.name) && Objects.equals(description, command.description) && Objects.equals(usage, command.usage) && Objects.equals(aliases, command.aliases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, usage, aliases);
    }

}
