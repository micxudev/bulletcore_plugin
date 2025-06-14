package org.dredd.bulletCore.utils;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
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