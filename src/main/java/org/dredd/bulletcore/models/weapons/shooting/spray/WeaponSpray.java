package org.dredd.bulletcore.models.weapons.shooting.spray;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

/**
 * Defines the parameters used to represent weapon spray.
 * <p>
 * The spray system models how weapon inaccuracy changes depending on the player's state.
 *
 * @author dredd
 * @see PlayerSprayContext
 * @since 1.0.0
 */
public final class WeaponSpray {

    // ----------< Static >----------

    // -----< Constants >-----

    /**
     * Minimum total spray value per {@link MovementState}, {@link MovementModifier}.
     */
    private static final double MIN_SPRAY = -45.0D;

    /**
     * Maximum total spray value allowed for {@link MovementState}, {@link MovementModifier}.<br>
     * This value is used to clamp both individual spray values loaded from config and the final total spray.
     */
    private static final double MAX_SPRAY = 45.0D;

    /**
     * Default spray value applied when {@link MovementState}, {@link MovementModifier} is not defined in config.<br>
     * Also used as the lower bound when clamping the total spray.
     */
    static final double NO_SPRAY = 0.0D;

    // -----< Loader >-----

    /**
     * Loads a {@link WeaponSpray} instance from the given YAML configuration.<br>
     * Spray values are loaded from keys using the prefixes {@code spray.state.} and {@code spray.modifier.}.
     * Each value is clamped between {@link #MIN_SPRAY} and {@link #MAX_SPRAY}; defaults to {@link #NO_SPRAY} if missing.
     *
     * @param config the YAML configuration to load from
     * @return a new {@link WeaponSpray} instance with the configured spray values
     */
    public static @NotNull WeaponSpray load(@NotNull YamlConfiguration config) {
        return new WeaponSpray(config);
    }

    /**
     * Loads clamped {@link Double} values for each enum constant from config.
     * <p>
     * Keys are {@code prefix + constant name (lowercase)}.<br>
     * Missing keys default to {@code NO_SPRAY}, and all values are clamped
     * to [{@code MIN_SPRAY}, {@code MAX_SPRAY}].
     *
     * @param <E>    the enum type
     * @param type   the enum class
     * @param config the config source
     * @param prefix the key prefix
     * @return an {@link EnumMap} of enum constants to their clamped values
     */
    private static <E extends Enum<E>> @NotNull EnumMap<E, Double> loadSprayValues(
        @NotNull Class<E> type,
        @NotNull YamlConfiguration config,
        @NotNull String prefix
    ) {
        final EnumMap<E, Double> map = new EnumMap<>(type);
        for (final E constant : type.getEnumConstants()) {
            final String path = prefix + constant.name().toLowerCase(Locale.ROOT);
            final double value = Math.clamp(config.getDouble(path, NO_SPRAY), MIN_SPRAY, MAX_SPRAY);
            map.put(constant, value);
        }
        return map;
    }


    // ----------< Instance >----------

    // -----< Attributes >-----

    /**
     * The minimum percentage of the maximum spray value that the random spray direction should be.<br>
     * If this value is 1.0 -> spray is always exactly 100%<br>
     * If this value is 0.5 -> spray is random from 50% to 100% of the maximum value<br>
     */
    final double minSprayPercent;

    /**
     * Weapon spray values associated with {@link MovementState}.
     */
    private final EnumMap<MovementState, Double> stateSpray;

    /**
     * Weapon spray values associated with {@link MovementModifier}.
     */
    private final EnumMap<MovementModifier, Double> modifierSpray;

    // -----< Construction >-----

    /**
     * Private constructor. Use {@link #load(YamlConfiguration)} instead.
     */
    private WeaponSpray(@NotNull YamlConfiguration config) {
        this.minSprayPercent = Math.clamp(config.getDouble("spray.minSprayPercent", 0.5D), 0.0D, 1.0D);
        this.stateSpray = loadSprayValues(MovementState.class, config, "spray.state.");
        this.modifierSpray = loadSprayValues(MovementModifier.class, config, "spray.modifier.");
    }

    // -----< Spray Retrieval API >-----

    /**
     * Calculates the total spray value based on the player's movement state and active modifiers.<br>
     * The result is clamped between [{@value NO_SPRAY} and {@value MAX_SPRAY}] to ensure valid bounds.
     *
     * @param movementState the current {@link MovementState} of the player
     * @param modifiers     a list of active {@link MovementModifier}s
     * @return the clamped total spray value for the given state and modifiers
     */
    public double getFinalValue(@NotNull MovementState movementState, @NotNull List<MovementModifier> modifiers) {
        final double total = getStateValue(movementState) + getModifiersValue(modifiers);
        return Math.clamp(total, NO_SPRAY, MAX_SPRAY);
    }

    /**
     * Returns the weapon spray value associated with the given {@link MovementState}.<br>
     * If no specific value is defined for the state, {@value NO_SPRAY} is returned.
     *
     * @param movementState the player movement state to query
     * @return the associated weapon spray value, or {@value NO_SPRAY} if not present
     */
    private double getStateValue(@NotNull MovementState movementState) {
        return stateSpray.getOrDefault(movementState, NO_SPRAY);
    }

    /**
     * Computes the total weapon spray value for a list of {@link MovementModifier}s.<br>
     * If any modifier has no defined spray value, it contributes {@value NO_SPRAY}.
     *
     * @param modifiers a list of active modifiers
     * @return the sum of spray values for all specified modifiers
     */
    private double getModifiersValue(@NotNull List<MovementModifier> modifiers) {
        return modifiers.stream()
            .mapToDouble(m -> modifierSpray.getOrDefault(m, NO_SPRAY))
            .sum();
    }
}