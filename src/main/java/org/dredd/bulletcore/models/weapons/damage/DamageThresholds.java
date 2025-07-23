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
 *   <li>If normalizedY > {@code head} → HEAD</li>
 *   <li>If normalizedY > {@code body} → BODY</li>
 *   <li>If normalizedY > {@code legs} → LEGS</li>
 *   <li>Else → FEET</li>
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

    /**
     * Loads damage thresholds from a config, using default values if not specified.
     * <p>
     * Thresholds are clamped between 0.0 and 1.0 to ensure validity.<br>
     * Values are expected under the {@code damage-thresholds} section in the config.
     *
     * <pre>{@code
     * damage-thresholds:
     *   head: 0.78
     *   body: 0.4
     *   legs: 0.08
     * }</pre>
     *
     * @param config the YAML configuration to load from
     * @return a new {@code DamageThresholds} instance populated from the config
     */
    public static DamageThresholds load(@NotNull FileConfiguration config) {
        return new DamageThresholds(
            getOrDefault(config, "head", 0.78),
            getOrDefault(config, "body", 0.4),
            getOrDefault(config, "legs", 0.08)
        );
    }

    /**
     * Retrieves damage thresholds YAML config, applying a default if missing,
     * and clamping it to a valid range.
     *
     * @param cfg the YAML configuration
     * @param key the body part key ({@code head}, {@code body}, {@code legs})
     * @param def the default value to use if not defined
     * @return the clamped damage threshold value
     */
    private static double getOrDefault(@NotNull FileConfiguration cfg, @NotNull String key, double def) {
        return Math.clamp(cfg.getDouble("damage-thresholds." + key, def), 0.0D, 1.0D);
    }
}