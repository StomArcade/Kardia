package net.bitbylogic.kardiavelocity.message.tag;

import net.bitbylogic.kardiavelocity.message.messages.BrandingMessages;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class BrandingTags {

    public static final TagResolver PRIMARY = TagResolver.resolver(
            "primary",
            (queue, ctx) -> Tag.inserting(BrandingMessages.PRIMARY_COLOR.get())
    );

    public static final TagResolver SECONDARY = TagResolver.resolver(
            "secondary",
            (queue, ctx) -> Tag.inserting(BrandingMessages.SECONDARY_COLOR.get())
    );

    public static final TagResolver HIGHLIGHT = TagResolver.resolver(
            "highlight",
            (queue, ctx) -> Tag.inserting(BrandingMessages.HIGHLIGHT_COLOR.get())
    );

    public static final TagResolver SEPARATOR = TagResolver.resolver(
            "separator",
            (queue, ctx) -> Tag.inserting(BrandingMessages.SEPARATOR_COLOR.get())
    );

    public static final TagResolver ERROR_PRIMARY = TagResolver.resolver(
            "error_primary",
            (queue, ctx) -> Tag.inserting(BrandingMessages.ERROR_PRIMARY_COLOR.get())
    );

    public static final TagResolver ERROR_SECONDARY = TagResolver.resolver(
            "error_secondary",
            (queue, ctx) -> Tag.inserting(BrandingMessages.ERROR_SECONDARY_COLOR.get())
    );

    public static final TagResolver ERROR_HIGHLIGHT = TagResolver.resolver(
            "error_highlight",
            (queue, ctx) -> Tag.inserting(BrandingMessages.ERROR_HIGHLIGHT_COLOR.get())
    );

    public static final TagResolver SUCCESS_PRIMARY = TagResolver.resolver(
            "success_primary",
            (queue, ctx) -> Tag.inserting(BrandingMessages.SUCCESS_PRIMARY_COLOR.get())
    );

    public static final TagResolver SUCCESS_SECONDARY = TagResolver.resolver(
            "success_secondary",
            (queue, ctx) -> Tag.inserting(BrandingMessages.SUCCESS_SECONDARY_COLOR.get())
    );

    public static final TagResolver SUCCESS_HIGHLIGHT = TagResolver.resolver(
            "success_highlight",
            (queue, ctx) -> Tag.inserting(BrandingMessages.SUCCESS_HIGHLIGHT_COLOR.get())
    );

    public static final TagResolver ALL =
            TagResolver.resolver(PRIMARY, SECONDARY, HIGHLIGHT, SEPARATOR, ERROR_PRIMARY, ERROR_SECONDARY,
                    ERROR_HIGHLIGHT, SUCCESS_PRIMARY, SUCCESS_SECONDARY, SUCCESS_HIGHLIGHT);

    private BrandingTags() {}

}
