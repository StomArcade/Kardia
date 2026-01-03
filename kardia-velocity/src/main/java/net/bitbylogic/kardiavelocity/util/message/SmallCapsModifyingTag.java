package net.bitbylogic.kardiavelocity.util.message;

import net.bitbylogic.utils.smallcaps.SmallCapsConverter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import org.jetbrains.annotations.NotNull;

public class SmallCapsModifyingTag implements Modifying {

    @Override
    public Component apply(@NotNull Component current, int depth) {
        if (current instanceof TextComponent textComponent) {
            String content = textComponent.content();
            String smallCaps = SmallCapsConverter.convert(content);
            return Component.text(smallCaps).style(textComponent.style());
        }

        return current;
    }

}
