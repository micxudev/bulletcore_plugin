package org.dredd.bulletcore.config.messages;

import net.kyori.adventure.text.format.Style;

import java.util.List;

/**
 * Holds the style definitions for a {@link TranslatableMessage}.
 *
 * @param keyStyle  the style applied to the translation key
 * @param argStyles the ordered list of styles for each message argument;
 *                  its size must match the number of arguments expected by the message
 * @author dredd
 * @since 1.0.0
 */
public record StylesBundle(
    Style keyStyle,
    List<Style> argStyles
) {}