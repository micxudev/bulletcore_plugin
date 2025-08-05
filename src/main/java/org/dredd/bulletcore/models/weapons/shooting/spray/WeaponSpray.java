package org.dredd.bulletcore.models.weapons.shooting.spray;

import org.bukkit.configuration.file.FileConfiguration;
import org.dredd.bulletcore.utils.EnumMapLoader;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Defines the parameters used to represent weapon spray.
 * <p>
 * The spray system models how weapon inaccuracy changes depending on the player's state.
 *
 * @author dredd
 * @see PlayerSprayContext
 * @since 1.0.0
 */
public class WeaponSpray {

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
    private static final double NO_SPRAY = 0.0D;

    /**
     * Weapon spray values associated with {@link MovementState}.
     */
    private final Map<MovementState, Double> stateSpray;

    /**
     * Weapon spray values associated with {@link MovementModifier}.
     */
    private final Map<MovementModifier, Double> modifierSpray;

    /**
     * Private constructor to initialize the state and modifier spray maps.
     *
     * @param stateSpray    map of spray values per {@link MovementState}
     * @param modifierSpray map of spray values per {@link MovementModifier}
     */
    private WeaponSpray(Map<MovementState, Double> stateSpray, Map<MovementModifier, Double> modifierSpray) {
        this.stateSpray = stateSpray;
        this.modifierSpray = modifierSpray;
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

    /**
     * Calculates the total spray value based on the player's movement state and active modifiers.<br>
     * The result is clamped between [{@value NO_SPRAY} and {@value MAX_SPRAY}] to ensure valid bounds.
     *
     * @param movementState the current {@link MovementState} of the player
     * @param modifiers     a list of active {@link MovementModifier}s
     * @return the clamped total spray value for the given state and modifiers
     */
    public double getFinalValue(@NotNull MovementState movementState, @NotNull List<MovementModifier> modifiers) {
        double total = getStateValue(movementState) + getModifiersValue(modifiers);
        return Math.clamp(total, NO_SPRAY, MAX_SPRAY);
    }

    /**
     * Loads a {@link WeaponSpray} instance from the given YAML configuration.<br>
     * Spray values are loaded from keys using the prefixes {@code spray.state.} and {@code spray.modifier.}.
     * Each value is clamped between {@link #MIN_SPRAY} and {@link #MAX_SPRAY}; defaults to {@link #NO_SPRAY} if missing.
     *
     * @param cfg the YAML configuration to load from
     * @return a new {@link WeaponSpray} instance with the configured spray values
     */
    public static @NotNull WeaponSpray load(@NotNull FileConfiguration cfg) {
        return new WeaponSpray(
            EnumMapLoader.loadDoubleMap(MovementState.class, cfg, "spray.state.", NO_SPRAY, MIN_SPRAY, MAX_SPRAY),
            EnumMapLoader.loadDoubleMap(MovementModifier.class, cfg, "spray.modifier.", NO_SPRAY, MIN_SPRAY, MAX_SPRAY)
        );
    }
}