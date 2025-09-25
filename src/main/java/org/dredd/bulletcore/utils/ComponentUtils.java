package org.dredd.bulletcore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.dredd.bulletcore.config.messages.TranslatableMessage;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.TextColor.color;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

/**
 * Utility class for working with Adventure {@link net.kyori.adventure.text.Component} elements.
 *
 * <p>Provides methods for styling text components with common defaults, like disabling italics or setting color.</p>
 *
 * @since 1.0.0
 */
public final class ComponentUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private ComponentUtils() {}

    /**
     * Default white text color used for neutral UI components.
     */
    public static final TextColor WHITE = color(226, 229, 240);

    /**
     * Key style used for {@link TranslatableMessage} as fallback.
     */
    public static final Style KEY_STYLE = deserialize("<!i><white>").style();

    /**
     * Argument styles used for {@link TranslatableMessage} as fallback.
     */
    public static final Style ARG_STYLE_RED = deserialize("<red>").style();
    public static final Style ARG_STYLE_GREEN = deserialize("<green>").style();
    public static final Style ARG_STYLE_BLUE = deserialize("<blue>").style();
    public static final Style ARG_STYLE_YELLOW = deserialize("<yellow>").style();

    /**
     * Creates a white, non-italic {@link TextComponent}.
     *
     * @param text the content
     * @return the styled component
     */
    public static @NotNull TextComponent plainWhite(@NotNull String text) {
        return text(text, WHITE).decoration(ITALIC, false);
    }

    /**
     * Deserializes a MiniMessage-formatted string into a {@link Component}.
     *
     * @param message the MiniMessage-formatted string
     * @return the deserialized {@link Component}
     */
    public static @NotNull Component deserialize(@NotNull String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }
}