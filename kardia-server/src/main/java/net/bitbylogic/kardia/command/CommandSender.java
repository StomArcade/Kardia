package net.bitbylogic.kardia.command;

public interface CommandSender {

    void sendMessage(String message);

    void sendWarning(String warning);

    void sendError(String error);
}
