package org.dredd.bulletCore.utils;

/**
 * Utility class for mathematical helper functions.
 *
 * @since 1.0.0
 */
public final class MathUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private MathUtils() {}

    /**
     * Clamps a given value between a minimum and maximum bound.
     *
     * @param value the value to clamp
     * @param min   the lower bound
     * @param max   the upper bound
     * @return {@code value} if within range, otherwise {@code min} or {@code max}
     */
    public static int clamp(int value, int min, int max) {
        return Math.min(max, Math.max(value, min));
    }
}