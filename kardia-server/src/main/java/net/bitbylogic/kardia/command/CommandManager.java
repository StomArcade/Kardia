package net.bitbylogic.kardia.command;

import com.google.common.collect.Lists;
import net.bitbylogic.kardia.Kardia;
import net.bitbylogic.kardia.command.impl.*;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandManager {

    private final List<Command> commands;
    private ExecutorService commandService;

    public CommandManager() {
        this.commands = Lists.newCopyOnWriteArrayList();

        registerCommand(
                new EndAllServerProcessesCommand(),
                new HelpCommand(),
                new InvalidateRedisCommand(),
                new ListContainerCommand(),
                new ServersCommand(),
                new ReloadAllPackagesCommand(),
                new ReloadPackageCommand(),
                new StartServerCommand(),
                new StopCommand(),
                new StopContainerCommand(),
                new StopServerCommand(),
                new PackagesCommand(),
                new DispatchCommand(),
                new ReloadConfigCommand()
        );

        init();
    }

    public void registerCommand(Command... commands) {
        this.commands.addAll(Arrays.asList(commands));
    }

    public Command getCommand(String command) {
        return this.commands.stream()
                .filter(c -> c.name().equalsIgnoreCase(command) || c.isAliasExist(command))
                .findFirst()
                .orElse(null);
    }

    public boolean executeCommand(CommandSender sender, String command) {
        String[] array = command.split(" ");
        if(array.length > 0) {
            String[] args = (array.length > 1 ? Arrays.copyOfRange(array, 1, array.length) : new String[0]);
            Command c = getCommand(array[0]);
            if(c != null) {
                c.execute(sender, args);
                return true;
            }
        }
        return false;
    }

    public void init() {
        this.commandService = Executors.newSingleThreadExecutor();
        this.commandService.submit(() -> {
            try {
                Scanner s = new Scanner(System.in);

                while(true) {
                    String command = s.nextLine();
                    try {
                        boolean executed = executeCommand(new CommandSender() {
                            @Override
                            public void sendMessage(String message) {
                                Kardia.LOGGER.info(message);
                            }

                            @Override
                            public void sendWarning(String warning) {
                                Kardia.LOGGER.warn(warning);
                            }

                            @Override
                            public void sendError(String error) {
                                Kardia.LOGGER.error(error);
                            }
                        }, command);

                        if(!executed)
                            Kardia.LOGGER.warn("Invalid command! Use \"help\" for help.");
                    } catch (Exception e) {
                        Kardia.LOGGER.error("Error executing command", e);
                    }
                }

            } catch (Exception e) {
                Kardia.LOGGER.error("Failed to start command service!", e);
            }
        });
    }

    public ExecutorService commandService() {
        return commandService;
    }

    public List<Command> commands() {
        return List.copyOf(commands);
    }

}
