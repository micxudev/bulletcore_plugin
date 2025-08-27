package org.dredd.bulletcore.models.weapons.shooting.spray;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Handles weapon spray for players.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class SprayHandler {

    /**
     * Stores the spray context data for each player.
     */
    private static final Map<UUID, PlayerSprayContext> playerSprayContext = new HashMap<>();

    /**
     * The minimum percentage of the maximum spray value that the random spray direction should be.
     */
    private static final double MIN_SPRAY_PERCENT = 0.5D;

    /**
     * Private constructor to prevent instantiation.
     */
    private SprayHandler() {}

    /**
     * Retrieves the existing or newly created {@link PlayerSprayContext} instance for the given player.<br>
     * Used to track spray state during the shots.
     *
     * @param player the player to get the spray context for
     * @return {@link PlayerSprayContext} instance for the given player
     */
    public static @NotNull PlayerSprayContext getSprayContext(@NotNull Player player) {
        return playerSprayContext.computeIfAbsent(player.getUniqueId(), k -> new PlayerSprayContext(player));
    }

    /**
     * Applies the spray logic for a shot fired by the given player.
     *
     * @param player           the player who fired the shot
     * @param weapon           the weapon used to fire the shot
     * @param initialDirection the initial direction of the shot
     * @return a list of size {@link Weapon#pelletsPerShot} where each element is the final direction of each pellet
     */
    public static @NotNull List<Vector> handleShot(@NotNull Player player, @NotNull Weapon weapon, @NotNull Vector initialDirection) {
        int pelletCount = weapon.pelletsPerShot;
        List<Vector> directions = new ArrayList<>(pelletCount);

        var sprayContext = getSprayContext(player);
        var state = sprayContext.getState();
        var modifiers = sprayContext.getModifiers();
        double finalSpray = weapon.spray.getFinalValue(state, modifiers);

        sprayContext.sendMessage(state, modifiers, finalSpray);

        if (finalSpray <= WeaponSpray.NO_SPRAY) {
            for (int i = 0; i < pelletCount; i++)
                directions.add(initialDirection);
            return directions;
        }

        // Change Shot Direction
        double maxSprayRadians = Math.toRadians(finalSpray);
        double minSprayRadians = maxSprayRadians * MIN_SPRAY_PERCENT;
        double cosMax = Math.cos(maxSprayRadians);
        double cosMin = Math.cos(minSprayRadians);

        for (int i = 0; i < pelletCount; i++) {
            double z = cosMin + (cosMax - cosMin) * Math.random();
            double sinT = Math.sqrt(1 - z * z);
            double theta = Math.TAU * Math.random();
            double x = sinT * Math.cos(theta);
            double y = sinT * Math.sin(theta);
            Vector offset = new Vector(x, y, z);
            Vector pelletDirection = rotateVector(offset, initialDirection).normalize();
            directions.add(pelletDirection);
        }

        return directions;
    }

    /**
     * Rotates a unit vector from a cone aligned with the Z+ axis
     * to be aligned around an arbitrary direction vector.
     *
     * @param offset    the unit vector within the spray cone, originally centered around the Z+ axis.
     * @param direction the direction vector that the spray cone should be centered around.
     * @return the new vector representing the rotated spray direction — the original offset,
     * reoriented so that it lies within a cone centered around the given direction vector.
     */
    private static Vector rotateVector(@NotNull Vector offset, @NotNull Vector direction) {
        Vector up = (direction.getX() == 0 && direction.getZ() == 0) // avoid gimbal lock
            ? new Vector(1, 0, 0)
            : new Vector(0, 1, 0);

        Vector right = direction.clone().crossProduct(up).normalize();
        Vector upAdjusted = right.clone().crossProduct(direction).normalize();

        return right.multiply(offset.getX())
            .add(upAdjusted.multiply(offset.getY()))
            .add(direction.clone().multiply(offset.getZ()));
    }

    /**
     * Clears the spray context for the given player.
     *
     * @param player the player whose spray context should be cleared
     */
    public static void clearSprayContext(@NotNull Player player) {
        playerSprayContext.remove(player.getUniqueId());
    }

    /**
     * Update the spray context for each player.
     */
    public static void tick() {
        playerSprayContext.values().forEach(PlayerSprayContext::tick);
    }
}