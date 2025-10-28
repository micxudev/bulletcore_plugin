package org.dredd.bulletcore.models.weapons.shooting.spray;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles weapon spray for players.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class SprayHandler {

    /**
     * Private constructor to prevent instantiation.
     */
    private SprayHandler() {}

    /**
     * Stores the spray context data for each player.
     */
    private static final Map<UUID, PlayerSprayContext> SPRAY_CONTEXTS = new HashMap<>();

    // -----< Public API >-----

    /**
     * Retrieves the existing or newly created {@link PlayerSprayContext} instance for the given player.<br>
     * Used to track spray state during the shots.
     *
     * @param player the player to get the spray context for
     * @return {@link PlayerSprayContext} instance for the given player
     */
    public static @NotNull PlayerSprayContext getSprayContext(@NotNull Player player) {
        return SPRAY_CONTEXTS.computeIfAbsent(player.getUniqueId(), k -> new PlayerSprayContext(player));
    }

    /**
     * Clears the spray context for the given player.
     *
     * @param player the player whose spray context should be cleared
     */
    public static void clearSprayContext(@NotNull Player player) {
        SPRAY_CONTEXTS.remove(player.getUniqueId());
    }

    /**
     * Update the spray context for each player.
     */
    public static void tick() {
        SPRAY_CONTEXTS.values().forEach(PlayerSprayContext::tick);
    }

    /**
     * Applies the spray logic for a shot fired by the given player.
     *
     * @param player           the player who fired the shot
     * @param weapon           the weapon used to fire the shot
     * @param initialDirection the initial direction of the shot
     * @return a Vector array of size {@link Weapon#pelletsPerShot} where each element is the final direction of each pellet
     */
    public static @NotNull Vector[] handleShot(@NotNull Player player,
                                               @NotNull Weapon weapon,
                                               @NotNull Vector initialDirection) {
        final Vector[] directions = new Vector[weapon.pelletsPerShot];

        final var sprayContext = getSprayContext(player);
        final var state = sprayContext.getState();
        final var modifiers = sprayContext.getModifiers();
        final double finalSpray = weapon.spray.getFinalValue(state, modifiers);

        sprayContext.sendMessage(state, modifiers, finalSpray);

        if (finalSpray <= WeaponSpray.NO_SPRAY) {
            Arrays.fill(directions, initialDirection);
            return directions;
        }

        // change shot direction
        final double maxSprayRadians = Math.toRadians(finalSpray);
        final double minSprayRadians = maxSprayRadians * weapon.spray.minSprayPercent;
        final double cosMax = Math.cos(maxSprayRadians);
        final double cosMin = Math.cos(minSprayRadians);

        for (int i = 0; i < directions.length; i++) {
            final double z = cosMin + (cosMax - cosMin) * Math.random();
            final double sinT = Math.sqrt(1 - z * z);
            final double theta = Math.TAU * Math.random();
            final double x = sinT * Math.cos(theta);
            final double y = sinT * Math.sin(theta);
            final Vector offset = new Vector(x, y, z);
            final Vector pelletDirection = rotateVector(offset, initialDirection).normalize();
            directions[i] = pelletDirection;
        }

        return directions;
    }

    // -----< Utilities >-----

    /**
     * Rotates a unit vector from a cone aligned with the Z+ axis
     * to be aligned around an arbitrary direction vector.
     *
     * @param offset    the unit vector within the spray cone, originally centered around the Z+ axis.
     * @param direction the direction vector that the spray cone should be centered around.
     * @return the new vector representing the rotated spray direction — the original offset,
     * reoriented so that it lies within a cone centered around the given direction vector.
     */
    private static @NotNull Vector rotateVector(@NotNull Vector offset,
                                                @NotNull Vector direction) {
        Vector up = (direction.getX() == 0.0D && direction.getZ() == 0.0D) // avoid gimbal lock
            ? new Vector(1.0D, 0.0D, 0.0D)
            : new Vector(0.0D, 1.0D, 0.0D);

        Vector right = direction.clone().crossProduct(up).normalize();
        Vector upAdjusted = right.clone().crossProduct(direction).normalize();

        return right.multiply(offset.getX())
            .add(upAdjusted.multiply(offset.getY()))
            .add(direction.clone().multiply(offset.getZ()));
    }
}