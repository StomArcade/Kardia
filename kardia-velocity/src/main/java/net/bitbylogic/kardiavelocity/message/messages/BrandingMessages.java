package net.bitbylogic.kardiavelocity.message.messages;

import net.bitbylogic.kardiavelocity.message.MessageGroup;
import net.bitbylogic.kardiavelocity.message.MessageKey;

public class BrandingMessages extends MessageGroup {

    public static MessageKey PRIMARY_COLOR;
    public static MessageKey SECONDARY_COLOR;
    public static MessageKey HIGHLIGHT_COLOR;
    public static MessageKey SEPARATOR_COLOR;

    public static MessageKey ERROR_PRIMARY_COLOR;
    public static MessageKey ERROR_SECONDARY_COLOR;
    public static MessageKey ERROR_HIGHLIGHT_COLOR;

    public static MessageKey SUCCESS_PRIMARY_COLOR;
    public static MessageKey SUCCESS_SECONDARY_COLOR;
    public static MessageKey SUCCESS_HIGHLIGHT_COLOR;

    public static MessageKey MESSAGE;
    public static MessageKey ERROR_MESSAGE;
    public static MessageKey SUCCESS_MESSAGE;

    public BrandingMessages() {
        super("Branding");
    }

    @Override
    public void register() {
        PRIMARY_COLOR = register("Colors.Primary", "<#90caf9>");
        SECONDARY_COLOR = register("Colors.Secondary", "<#bbdefb>");
        HIGHLIGHT_COLOR = register("Colors.Highlight", "<#64b5f6>");
        SEPARATOR_COLOR = register("Colors.Separator", "<#555555>");

        ERROR_PRIMARY_COLOR = register("Colors.Error.Primary", "<#ff758f>");
        ERROR_SECONDARY_COLOR = register("Colors.Error.Secondary", "<#ffb3c1>");
        ERROR_HIGHLIGHT_COLOR = register("Colors.Error.Highlight", "<#ff4d6d>");

        SUCCESS_PRIMARY_COLOR = register("Colors.Success.Primary", "<#3dd67b>");
        SUCCESS_SECONDARY_COLOR = register("Colors.Success.Secondary", "<#7be4a5>");
        SUCCESS_HIGHLIGHT_COLOR = register("Colors.Success.Highlight", "<#00c851>");

        MESSAGE = register("Message", "<primary><header> <separator>☰ <secondary><message>");
        ERROR_MESSAGE = register("Error-Message", "<error_primary><header> <separator>☰ <error_secondary><message>");
        SUCCESS_MESSAGE = register("Success-Message", "<success_primary><header> <separator>☰ <success_secondary><message>");
    }

}
