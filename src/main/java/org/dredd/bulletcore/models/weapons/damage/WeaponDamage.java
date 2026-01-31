package org.dredd.bulletcore.models.weapons.damage;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the amount of damage dealt by a weapon.
 *
 * @param head   damage value when hitting the {@link DamagePoint#HEAD}
 * @param body   damage value when hitting the {@link DamagePoint#BODY}
 * @param legs   damage value when hitting the {@link DamagePoint#LEGS}
 * @param feet   damage value when hitting the {@link DamagePoint#FEET}
 * @param shield damage value when hitting the blocking {@link Material#SHIELD}
 * @author dredd
 * @since 1.0.0
 */
public record WeaponDamage(
    double head,
    double body,
    double legs,
    double feet,
    double shield
) {

    // ----------< Static Loader >----------

    /**
     * Loads weapon damages from a YAML configuration.
     * <p>
     * Values expected in a {@code damage} section with the keys
     * {@code head}, {@code body}, {@code legs}, {@code feet}, {@code shield}.
     * <p>
     * Example section structure:
     * <pre>{@code
     * damage:
     *   head: 20.0
     *   body: 12.0
     *   legs: 8.5
     *   feet: 4.5
     *   shield: 50.0
     * }</pre>
     *
     * @param config the YAML configuration to load from
     * @return a new {@link WeaponDamage} instance
     */
    public static @NotNull WeaponDamage load(@NotNull YamlConfiguration config) {
        return new WeaponDamage(
            getOrDefault(config, "head", 20.0D),
            getOrDefault(config, "body", 12.0D),
            getOrDefault(config, "legs", 8.5D),
            getOrDefault(config, "feet", 4.5D),
            getOrDefault(config, "shield", 50.0D)
        );
    }

    // ----------< Utilities >----------

    /**
     * Retrieves a damage value from the YAML config, applying a default if missing,
     * and clamping it to a valid range.
     *
     * @param config the YAML configuration
     * @param key    the part key
     * @param def    the default value to use if not defined
     * @return the clamped damage value from the config
     */
    private static double getOrDefault(@NotNull YamlConfiguration config,
                                       @NotNull String key,
                                       double def) {
        return Math.clamp(config.getDouble("damage." + key, def), 0.0D, 99999.0D);
    }
}