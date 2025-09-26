package org.dredd.bulletcore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Utility for creating and formatting {@link Component} instances.
 *
 * @since 1.0.0
 */
public final class ComponentUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private ComponentUtils() {}

    /**
     * Neutral white color used for UI text.
     */
    public static final TextColor WHITE = TextColor.color(226, 229, 240);

    /**
     * Predefined non-italic white style.
     */
    public static final Style PLAIN_WHITE_STYLE = Style.style(WHITE, TextDecoration.ITALIC.withState(false));

    /**
     * Creates a white, non-italic {@link TextComponent} with the given content.
     *
     * @param text the content
     * @return a styled text component
     */
    public static @NotNull TextComponent plainWhite(@NotNull String text) {
        return Component.text(text, PLAIN_WHITE_STYLE);
    }

    /**
     * Parses a MiniMessage string into a {@link Component}.
     *
     * @param message MiniMessage-formatted input
     * @return the parsed component
     */
    public static @NotNull Component deserialize(@NotNull String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }
}