package org.dredd.bulletcore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.dredd.bulletcore.config.messages.TranslatableMessages;
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
     * Pre-configured instance of {@link MiniMessage} for parsing MiniMessage formatted strings.
     */
    public static final MiniMessage MINI = MiniMessage.miniMessage();

    /**
     * Private constructor to prevent instantiation.
     */
    private ComponentUtils() {}

    /**
     * Default white text color used for neutral UI components.
     */
    public static final TextColor WHITE = color(226, 229, 240);

    /**
     * Key style used for {@link TranslatableMessages} as fallback.
     */
    public static final Component KEY_STYLE = MINI.deserialize("<!i><white>");

    /**
     * Argument styles used for {@link TranslatableMessages} as fallback.
     */
    public static final Component ARG_STYLE_RED = MINI.deserialize("<red>");
    public static final Component ARG_STYLE_GREEN = MINI.deserialize("<green>");
    public static final Component ARG_STYLE_BLUE = MINI.deserialize("<blue>");
    public static final Component ARG_STYLE_YELLOW = MINI.deserialize("<yellow>");

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
}