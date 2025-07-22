package org.dredd.bulletcore.utils;

import java.util.concurrent.ThreadLocalRandom;

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
        if (min > max)
            throw new IllegalArgumentException("min must be ≤ max");

        return Math.min(max, Math.max(value, min));
    }

    public static double clamp(double value, double min, double max) {
        if (min > max)
            throw new IllegalArgumentException("min must be ≤ max");

        return Math.min(max, Math.max(value, min));
    }

    public static long clamp(long value, long min, long max) {
        if (min > max)
            throw new IllegalArgumentException("min must be ≤ max");

        return Math.min(max, Math.max(value, min));
    }

    public static float clamp(float value, float min, float max) {
        if (min > max)
            throw new IllegalArgumentException("min must be ≤ max");

        return Math.min(max, Math.max(value, min));
    }

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
     * Linearly interpolates between two values.
     *
     * @param a The first value
     * @param b The second value
     * @param t The interpolation value, clamped between 0 and 1
     * @return The interpolated value
     */
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * clamp(t, 0.0f, 1.0f);
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