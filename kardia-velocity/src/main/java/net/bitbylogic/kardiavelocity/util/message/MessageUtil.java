package net.bitbylogic.kardiavelocity.util.message;

import net.bitbylogic.kardiavelocity.message.messages.BrandingMessages;
import net.bitbylogic.kardiavelocity.message.tag.BrandingTags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MessageUtil {

    private static final TagResolver SMALL_CAPS = TagResolver.resolver(
            "smallcaps",
            (queue, ctx) -> new SmallCapsModifyingTag()
    );

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder()
            .tags(
                    TagResolver.builder()
                            .resolver(StandardTags.defaults())
                            .resolvers(BrandingTags.ALL)
                            .resolvers(SMALL_CAPS)
                            .build()
            ).build();

    public static String serialize(@NotNull Component component) {
        return MINI_MESSAGE.serialize(component);
    }

    public static Component deserialize(@NotNull String message, TagResolver.Single... placeholders) {
        return MINI_MESSAGE.deserialize(message, placeholders);
    }

    public static Component primary(@NotNull String message, TagResolver.Single... placeholders) {
        return branding(BrandingMessages.MESSAGE.getPlain(), message, placeholders);
    }

    public static Component primary(@NotNull String header, @NotNull String message, TagResolver.Single... placeholders) {
        return branding(BrandingMessages.MESSAGE.getPlain(), header, message, placeholders);
    }

    public static Component error(@NotNull String message, TagResolver.Single... placeholders) {
        return branding(BrandingMessages.ERROR_MESSAGE.getPlain(), message, placeholders);
    }

    public static Component error(@NotNull String header, @NotNull String message, TagResolver.Single... placeholders) {
        return branding(BrandingMessages.ERROR_MESSAGE.getPlain(), header, message, placeholders);
    }

    public static Component success(@NotNull String header, @NotNull String message, TagResolver.Single... placeholders) {
        return branding(BrandingMessages.SUCCESS_MESSAGE.getPlain(), header, message, placeholders);
    }

    public static Component success(@NotNull String message, TagResolver.Single... placeholders) {
        return branding(BrandingMessages.SUCCESS_MESSAGE.getPlain(), message, placeholders);
    }

    private static Component branding(@NotNull String brandingMessage, @NotNull String message, TagResolver.Single... placeholders) {
        List<TagResolver.Single> finalPlaceholders = new ArrayList<>(List.of(placeholders));
        finalPlaceholders.add(Placeholder.component("header", Component.text("Stom Arcade")));
        finalPlaceholders.add(Placeholder.parsed("message", message));

        return MINI_MESSAGE.deserialize(brandingMessage, finalPlaceholders.toArray(TagResolver.Single[]::new));
    }

    private static Component branding(@NotNull String brandingMessage, @NotNull String header, @NotNull String message, TagResolver.Single... placeholders) {
        List<TagResolver.Single> finalPlaceholders = new ArrayList<>(List.of(placeholders));
        finalPlaceholders.add(Placeholder.parsed("header", header));
        finalPlaceholders.add(Placeholder.parsed("message", message));

        return MINI_MESSAGE.deserialize(brandingMessage, finalPlaceholders.toArray(TagResolver.Single[]::new));
    }

}
