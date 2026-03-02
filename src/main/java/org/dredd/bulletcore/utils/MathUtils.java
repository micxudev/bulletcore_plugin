package org.dredd.bulletcore.utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class for math.
 *
 * @since 1.0.0
 */
public final class MathUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private MathUtils() {}

    /**
     * Returns a random number between the mean and the mean plus or minus the variance.
     *
     * @param mean     The mean value
     * @param variance The variance
     * @return The random number
     */
    public static float variance(float mean, float variance) {
        if (variance < 0.0f)
            throw new IllegalArgumentException("variance must be non-negative");

        return mean + (ThreadLocalRandom.current().nextFloat() * 2.0f - 1.0f) * variance;
    }

    /**
     * Linearly interpolates between two values and clamps the interpolation
     * factor to the range {@code [0, 1]}.
     *
     * @param a The start value
     * @param b The end value
     * @param t The interpolation factor
     * @return The interpolated value clamped between {@code a} and {@code b}
     */
    public static float clampedLerp(float a, float b, float t) {
        if (t <= 0.0f) return a;
        if (t >= 1.0f) return b;
        return a + (b - a) * t;
    }

    /**
     * Determines whether a floating-point value is effectively zero within a specified tolerance.
     *
     * @param a       The number to evaluate.
     * @param epsilon The threshold under which the number is considered to be approximately zero.
     * @return True if the absolute value of {@code a} is less than {@code epsilon}; false otherwise.
     */
    public static boolean approximatelyZero(float a, float epsilon) {
        return Math.abs(a) < epsilon;
    }
}