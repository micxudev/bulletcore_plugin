package org.dredd.bulletcore.models.weapons.damage;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the normalized Y-thresholds used to classify the body part hit on a player.
 * <p>
 * These thresholds are used to determine {@link DamagePoint}s for a hit.
 *
 * <p>
 * Example threshold mapping:
 * <ul>
 *   <li>If normalizedY > {@code head} → {@link DamagePoint#HEAD}</li>
 *   <li>If normalizedY > {@code body} → {@link DamagePoint#BODY}</li>
 *   <li>If normalizedY > {@code legs} → {@link DamagePoint#LEGS}</li>
 *   <li>Else → {@link DamagePoint#FEET}</li>
 * </ul>
 * </p>
 *
 * @author dredd
 * @since 1.0.0
 */
public record DamageThresholds(
    double head,
    double body,
    double legs
) {

    // ----------< Static Loader >----------

    /**
     * Loads damage thresholds from a file configuration.
     * <p>
     * Values expected in a {@code damage-thresholds} section with the keys
     * {@code head}, {@code body}, {@code legs}.
     * <p>
     * Example section structure:
     * <pre>{@code
     * damage-thresholds:
     *   head: 0.78
     *   body: 0.4
     *   legs: 0.08
     * }</pre>
     *
     * @param config the File configuration to load from
     * @return a new {@link DamageThresholds} instance
     */
    public static @NotNull DamageThresholds load(@NotNull FileConfiguration config) {
        return new DamageThresholds(
            getOrDefault(config, "head", 0.78D),
            getOrDefault(config, "body", 0.4D),
            getOrDefault(config, "legs", 0.08D)
        );
    }

    // ----------< Utilities >----------

    /**
     * Retrieves damage thresholds from the File config, applying a default if missing,
     * and clamping it to a valid range.
     *
     * @param config the File configuration
     * @param key    the body part key ({@code head}, {@code body}, {@code legs})
     * @param def    the default value to use if not defined
     * @return the clamped damage threshold value
     */
    private static double getOrDefault(@NotNull FileConfiguration config,
                                       @NotNull String key,
                                       double def) {
        return Math.clamp(config.getDouble("damage-thresholds." + key, def), 0.0D, 1.0D);
    }
}