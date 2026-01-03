package net.bitbylogic.kardiavelocity.message;

import net.bitbylogic.kardiavelocity.KardiaVelocity;

import java.util.List;

public abstract class MessageGroup implements MessageRegistry {

    private final String pathPrefix;

    public MessageGroup(String pathPrefix) {
        this.pathPrefix = pathPrefix.endsWith(".") ? pathPrefix : pathPrefix + ".";
    }

    protected MessageKey register(String key, String defaultValue) {
        return KardiaVelocity.getInstance().getMessageManager().register(pathPrefix + key, defaultValue);
    }

    protected MessageKey register(String key, List<String> defaultValues) {
        return KardiaVelocity.getInstance().getMessageManager().register(pathPrefix + key, defaultValues);
    }

}
