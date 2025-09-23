package org.dredd.bulletcore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.dredd.bulletcore.config.messages.TranslatableMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public static final Component KEY_STYLE = deserialize("<!i><white>");

    /**
     * Argument styles used for {@link TranslatableMessage} as fallback.
     */
    public static final Component ARG_STYLE_RED = deserialize("<red>");
    public static final Component ARG_STYLE_GREEN = deserialize("<green>");
    public static final Component ARG_STYLE_BLUE = deserialize("<blue>");
    public static final Component ARG_STYLE_YELLOW = deserialize("<yellow>");

    /**
     * Returns a non-italic {@link TextComponent} with the given content and color.
     *
     * @param content the text to display
     * @param color   the color to apply (nullable; if {@code null}, default color is used)
     * @return a non-italic {@link TextComponent}
     */
    public static @NotNull TextComponent noItalic(@NotNull String content, @Nullable TextColor color) {
        return text(content, color).decoration(ITALIC, false);
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