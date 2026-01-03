package net.bitbylogic.kardiavelocity.util.context;

import com.velocitypowered.api.proxy.Player;
import net.bitbylogic.utils.context.ContextKey;
import net.bitbylogic.utils.reflection.TypeToken;

import java.util.Locale;

public class ContextKeys {

    // Velocity
    public static final ContextKey<Player> PLAYER = ContextKey.key("player", Player.class, Player.class);

    // Messages
    public static final ContextKey<Locale> LOCALE = ContextKey.key("locale", Locale.class, (new TypeToken<Locale>() {}).getType());

}
