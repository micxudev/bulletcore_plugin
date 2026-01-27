package org.dredd.bulletcore.utils;

import java.text.DecimalFormat;

import org.jetbrains.annotations.NotNull;

/**
 * Utility class for formatting numbers.
 *
 * @since 1.0.0
 */
public final class FormatterUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private FormatterUtils() {}

    /**
     * Decimal format with one optional decimal place.
     */
    private static final DecimalFormat NUMBER_FORMAT_ONE_DECIMAL = new DecimalFormat("#.#");

    /**
     * Decimal format with two optional decimal places.
     */
    private static final DecimalFormat NUMBER_FORMAT_TWO_DECIMAL = new DecimalFormat("#.##");

    /**
     * Formats a double as a percentage to 2 decimal places.
     * <p>
     * Format examples:
     * <pre>
     * 0.0    -> 0
     * 0.0375 -> 3.75
     * 0.125  -> 12.5
     * 0.25   -> 25
     * 0.5    -> 50
     * 0.7979 -> 79.79
     * 1.0    -> 100
     * </pre>
     *
     * @param ratio the ratio to format.
     * @return the formatted percentage.
     */
    public static @NotNull String formatPercent(double ratio) {
        return NUMBER_FORMAT_TWO_DECIMAL.format(ratio * 100);
    }

    /**
     * Formats a double to one decimal place.
     * <p>
     * Format examples:
     * <pre>
     * -0.52    -> -0.5
     * 0.0      -> 0
     * 0.333    -> 0.3
     * 1.19     -> 1.2
     * 123.4444 -> 123.4
     * </pre>
     *
     * @param value the value to format
     * @return the formatted value
     */
    public static @NotNull String formatDouble(double value) {
        return NUMBER_FORMAT_ONE_DECIMAL.format(value);
    }

    /**
     * Formats a double to two decimal places.
     *
     * @param value the value to format
     * @return the formatted value
     */
    public static @NotNull String formatDouble2(double value) {
        return NUMBER_FORMAT_TWO_DECIMAL.format(value);
    }

    /**
     * Formats an array of doubles.
     * <p>
     * Format examples:
     * <pre>
     * [10.0, 5.25, 3.125, 2.71828] -> ["10", "5.2", "3.1", "2.7"]
     * </pre>
     *
     * @param values the values to format
     * @return the formatted values array
     */
    public static @NotNull String[] formatDoubles(double... values) {
        final String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++)
            result[i] = formatDouble(values[i]);
        return result;
    }
}