package org.dredd.bulletcore.models.weapons.shooting.spray;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

/**
 * Represents additional movement-related conditions that affect the player's state.
 * <p>
 * Unlike {@link MovementState}, a player can have multiple {@code MovementModifier}s at once.
 * <p>
 * These modifiers used to adjust weapon spray in combination with the base movement state.
 *
 * @author dredd
 * @see MovementState
 * @since 1.0.0
 */
public enum MovementModifier {

    SPRINTING(NamedTextColor.RED),
    SNEAKING(NamedTextColor.GREEN),
    UNDERWATER(NamedTextColor.BLUE),
    IN_WATER(NamedTextColor.AQUA),
    IN_VEHICLE(NamedTextColor.DARK_PURPLE),
    IN_FLIGHT(NamedTextColor.GOLD),
    IN_CRAWLING_POSE(NamedTextColor.LIGHT_PURPLE),
    ON_CLIMBABLE(NamedTextColor.YELLOW);

    /**
     * A colored text component used in debug messages.
     */
    public final Component asComponent;

    MovementModifier(@NotNull TextColor color) {
        this.asComponent = Component.text(name(), color);
    }
}