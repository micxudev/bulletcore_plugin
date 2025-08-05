package org.dredd.bulletcore.models.weapons.shooting.spray;

/**
 * Represents the primary movement state of a player.
 * <p>
 * A player can only be in one {@code MovementState} at a time.
 * <p>
 * This state is determined based on the player's current posture or movement condition.
 * <p>
 * This enum is typically used in spray calculation logic to
 * determine weapon inaccuracy.
 *
 * @author dredd
 * @see MovementModifier
 * @see PlayerSprayContext
 * @since 1.0.0
 */
public enum MovementState {
    GLIDING,
    SWIMMING,
    JUMPING,
    STANDING,
    CRAWLING,
    RIDING,
    FLYING,
    CLIMBING,
    WALKING
}