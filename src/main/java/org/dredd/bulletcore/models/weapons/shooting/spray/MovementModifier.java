package org.dredd.bulletcore.models.weapons.shooting.spray;

import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.format.TextColor.color;

/**
 * Represents additional movement-related conditions that affect the player's state.
 * <p>
 * Unlike {@link MovementState}, a player can have multiple {@code MovementModifier}s at once.
 * <p>
 * These modifiers used to adjust weapon spray in combination with the base movement state.
 * <p>
 * Each modifier is associated with a display {@link TextColor} used for debug messages or UI.
 *
 * @author dredd
 * @see MovementState
 * @since 1.0.0
 */
public enum MovementModifier {
    SPRINTING(color(0xFF0000)),
    SNEAKING(color(0x00FF00)),
    UNDERWATER(color(0x0000FF)),
    IN_WATER(color(0x00FFFF)),
    IN_VEHICLE(color(0xBA23F6)),
    IN_FLIGHT(color(0xFFA500)),
    IN_CRAWLING_POSE(color(0xFF60FF)),
    ON_CLIMBABLE(color(0xFFFF00));

    /**
     * A display color used for this modifier in debug messages or UI.
     */
    final TextColor color;

    MovementModifier(@NotNull TextColor color) {
        this.color = color;
    }
}