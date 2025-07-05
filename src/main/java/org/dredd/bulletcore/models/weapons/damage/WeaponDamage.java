package org.dredd.bulletcore.models.weapons.damage;

import org.bukkit.configuration.file.YamlConfiguration;
import org.dredd.bulletcore.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the amount of damage dealt to different body parts by a weapon.
 * <p>
 * Example YAML structure:
 * </p>
 * <pre>{@code
 * damage:
 *   head: 20.7
 *   body: 10.5
 *   legs: 7.2
 *   feet: 3.5
 * }</pre>
 *
 * @param head damage value when hitting the head
 * @param body damage value when hitting the body (torso)
 * @param legs damage value when hitting the legs (thighs, knees)
 * @param feet damage value when hitting the feet or lower legs
 * @author dredd
 * @since 1.0.0
 */
public record WeaponDamage(
    double head,
    double body,
    double legs,
    double feet
) {

    /**
     * Loads a {@link WeaponDamage} instance from a YAML configuration section.
     *
     * <p>The configuration is expected to contain a {@code damage} section with keys
     * for {@code head}, {@code body}, {@code legs}, and {@code feet}.
     * If a value is missing, a default will be used:</p>
     * <ul>
     *     <li>{@code head}: 10.0</li>
     *     <li>{@code body}: 5.0</li>
     *     <li>{@code legs}: 3.0</li>
     *     <li>{@code feet}: 2.0</li>
     * </ul>
     *
     * @param weaponConfig the YAML configuration to load from
     * @return a new {@link WeaponDamage} instance populated from config
     */
    public static WeaponDamage load(@NotNull YamlConfiguration weaponConfig) {
        return new WeaponDamage(
            getOrDefault(weaponConfig, "head", 10),
            getOrDefault(weaponConfig, "body", 5),
            getOrDefault(weaponConfig, "legs", 3),
            getOrDefault(weaponConfig, "feet", 2)
        );
    }

    /**
     * Retrieves a damage value from the YAML config, applying a default if missing,
     * and clamping it to a valid range.
     *
     * @param cfg the YAML configuration
     * @param key the body part key (e.g. {@code head}, {@code body}, {@code legs}, {@code feet})
     * @param def the default value to use if not defined
     * @return the clamped damage value from the config
     */
    private static double getOrDefault(@NotNull YamlConfiguration cfg, @NotNull String key, double def) {
        return MathUtils.clamp(cfg.getDouble("damage." + key, def), 1, Double.MAX_VALUE);
    }
}