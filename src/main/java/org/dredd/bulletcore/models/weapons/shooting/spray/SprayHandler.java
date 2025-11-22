package org.dredd.bulletcore.models.weapons.shooting.spray;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.jetbrains.annotations.NotNull;

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

    // ----------< Spray Look-Up Table >----------
    private static final int LUT_SIZE = 256;

    private static final double[] SIN = new double[LUT_SIZE];

    private static final double[] COS = new double[LUT_SIZE];

    static {
        for (int i = 0; i < LUT_SIZE; i++) {
            final double a = (i / (double) LUT_SIZE) * Math.TAU;
            SIN[i] = Math.sin(a);
            COS[i] = Math.cos(a);
        }
    }

    /**
     * Stores the spray context data for each player.
     */
    private static final Map<UUID, PlayerSprayContext> SPRAY_CONTEXTS = new HashMap<>();

    // ----------< Public API >----------

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
     * @param initialDirection the normalized initial direction of the shot
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

        // -----< change shot direction >-----

        // calculate cone limits in radians and cos space
        final double maxSprayRadians = Math.toRadians(finalSpray);
        final double minSprayRadians = maxSprayRadians * weapon.spray.minSprayPercent;
        final double cosMax = Math.cos(maxSprayRadians);
        final double cosMin = Math.cos(minSprayRadians);
        final double cosRange = cosMax - cosMin;

        // build orthonormal basis

        // forward = initialDirection
        double fx = initialDirection.getX();
        double fy = initialDirection.getY();
        double fz = initialDirection.getZ();

        // avoid gimbal lock
        final boolean forwardIsVertical = Math.abs(fx) < 1e-12 && Math.abs(fz) < 1e-12;
        final double up0x = forwardIsVertical ? 1.0 : 0.0;
        final double up0y = forwardIsVertical ? 0.0 : 1.0;
        final double up0z = 0.0;

        // calculate right = forward × tmpUp
        double rx = fy * up0z - fz * up0y;
        double ry = fz * up0x - fx * up0z;
        double rz = fx * up0y - fy * up0x;

        // normalize right
        final double rLenSq = rx * rx + ry * ry + rz * rz;
        final double invRLen = 1.0 / Math.sqrt(rLenSq);
        rx *= invRLen;
        ry *= invRLen;
        rz *= invRLen;

        // calculate up = right × forward
        final double ux = ry * fz - rz * fy;
        final double uy = rz * fx - rx * fz;
        final double uz = rx * fy - ry * fx;

        final ThreadLocalRandom rng = ThreadLocalRandom.current();

        for (int i = 0; i < directions.length; i++) {
            // sample a random direction inside the cone (local coordinates)
            final double z = cosMin + cosRange * rng.nextDouble();
            final double sinT = Math.sqrt(1.0 - z * z);

            final int idx = rng.nextInt(LUT_SIZE);
            final double x = sinT * COS[idx];
            final double y = sinT * SIN[idx];

            // rotate the local offset into world coordinates
            final double dx = rx * x + ux * y + fx * z;
            final double dy = ry * x + uy * y + fy * z;
            final double dz = rz * x + uz * y + fz * z;

            directions[i] = new Vector(dx, dy, dz);
        }

        return directions;
    }
}