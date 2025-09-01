package org.dredd.bulletcore.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Locale;

/**
 * Utility class for loading {@link EnumMap} of different types from config.
 *
 * @since 1.0.0
 */
public final class EnumMapLoader {

    /**
     * Private constructor to prevent instantiation.
     */
    private EnumMapLoader() {}

    /**
     * Loads an {@link EnumMap} of {@link Double} values for the given enum type from a config.
     * <p>
     * Each value is read using the key {@code prefix + constant name (lowercase)}, and clamped
     * to the range [{@code min}, {@code max}]. Defaults to {@code def} if the key is missing.
     *
     * @param <E>       the enum type
     * @param enumClass the enum class
     * @param cfg       the configuration source
     * @param prefix    the key prefix in the config
     * @param def       the default value if a key is missing
     * @param min       the minimum allowed value
     * @param max       the maximum allowed value
     * @return an {@link EnumMap} of enum constants and their clamped values
     */
    public static <E extends Enum<E>> @NotNull EnumMap<E, Double> loadDoubleMap(
        @NotNull Class<E> enumClass,
        @NotNull FileConfiguration cfg,
        @NotNull String prefix,
        double def,
        double min,
        double max
    ) {
        EnumMap<E, Double> map = new EnumMap<>(enumClass);
        for (E constant : enumClass.getEnumConstants()) {
            String path = prefix + constant.name().toLowerCase(Locale.ROOT);
            double value = Math.clamp(cfg.getDouble(path, def), min, max);
            map.put(constant, value);
        }
        return map;
    }
}