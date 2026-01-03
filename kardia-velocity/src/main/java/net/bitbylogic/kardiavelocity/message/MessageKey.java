package net.bitbylogic.kardiavelocity.message;

import com.velocitypowered.api.proxy.Player;
import net.bitbylogic.kardiavelocity.util.context.ContextKeys;
import net.bitbylogic.kardiavelocity.util.message.MessageUtil;
import net.bitbylogic.utils.context.Context;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MessageKey {

    private final @NotNull String path;
    private final @NotNull Map<Locale, List<String>> values;

    public MessageKey(@NotNull String path, @NotNull String defaultValue) {
        this.path = path;
        this.values = new HashMap<>();

        values.put(Locale.ENGLISH, new ArrayList<>(List.of(defaultValue)));
    }

    public MessageKey(@NotNull String path, @NotNull List<String> defaultValue) {
        this.path = path;
        this.values = new HashMap<>();

        values.put(Locale.ENGLISH, new ArrayList<>(defaultValue));
    }

    public List<String> values(@NotNull Locale locale) {
        return values.computeIfAbsent(locale, k -> new ArrayList<>());
    }

    public Component get(@NotNull Context context) {
        Locale locale = context.getOrDefault(ContextKeys.LOCALE, Locale.ENGLISH);

        return get(locale);
    }

    public Component get(Audience audience) {
        Locale locale = audience instanceof Player player && player.getEffectiveLocale() != null ? player.getEffectiveLocale() : Locale.ENGLISH;

        return get(locale);
    }

    public Component get(@NotNull Player player, TagResolver.Single... modifiers) {
        Locale locale = player.getEffectiveLocale() != null ? player.getEffectiveLocale() : Locale.ENGLISH;

        return get(locale, modifiers);
    }

    public Component get(@NotNull Locale locale, TagResolver.Single... modifiers) {
        return MessageUtil.deserialize(values.getOrDefault(locale, values.get(Locale.ENGLISH)).getFirst(), modifiers);
    }

    public Component get(TagResolver.Single... modifiers) {
        return MessageUtil.deserialize(values.get(Locale.ENGLISH).getFirst(), modifiers);
    }

    public List<Component> getAll(TagResolver.Single... modifiers) {
        List<Component> components = new ArrayList<>();

        for (String string : values.get(Locale.ENGLISH)) {
            components.add(MessageUtil.deserialize(string, modifiers));
        }

        return components;
    }

    public String getPlain() {
        return values.get(Locale.ENGLISH).getFirst();
    }

    public List<String> getPlainValues() {
        return values.get(Locale.ENGLISH);
    }

    public void send(@NotNull Context context, TagResolver.Single... modifiers) {
        Player player = context.get(ContextKeys.PLAYER).orElse(null);

        if (player == null) {
            return;
        }

        Locale locale = context.getOrDefault(ContextKeys.LOCALE, Locale.ENGLISH);

        send(player, locale, modifiers);
    }

    public void send(@NotNull Audience audience, TagResolver.Single... modifiers) {
        send(audience, Locale.ENGLISH, modifiers);
    }

    public void send(@NotNull Audience audience, @NotNull Locale locale, TagResolver.Single... modifiers) {
        values.getOrDefault(locale, values.get(Locale.ENGLISH)).forEach(message -> audience.sendMessage(MessageUtil.deserialize(message, modifiers)));
    }

    public @NotNull String path() {
        return path;
    }

    public @NotNull Map<Locale, List<String>> values() {
        return values;
    }

}
