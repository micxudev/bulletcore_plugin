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
     * A small value used to compare floating point numbers.
     */
    private static final float EPSILON = 1e-6f;

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
     * Checks if two floating point numbers are approximately equal using a custom epsilon.
     *
     * @param a       The first number
     * @param b       The second number
     * @param epsilon The tolerance value
     * @return True if the numbers are approximately equal
     */
    public static boolean approximately(float a, float b, float epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    /**
     * Checks if two floating point numbers are approximately equal using default epsilon.
     *
     * @param a The first number
     * @param b The second number
     * @return True if the numbers are approximately equal
     */
    public static boolean approximately(float a, float b) {
        return approximately(a, b, EPSILON);
    }

    /**
     * Moves a value {@code current} towards {@code target}.
     *
     * @param current  The current value
     * @param target   The target value
     * @param maxDelta The maximum change that should be applied
     * @return The new value
     * @throws IllegalArgumentException if maxDelta is negative or zero
     */
    public static float moveTowards(float current, float target, float maxDelta) {
        if (maxDelta <= 0.0f)
            throw new IllegalArgumentException("maxDelta must be positive");

        if (Math.abs(target - current) <= maxDelta) return target;
        return current + Math.signum(target - current) * maxDelta;
    }
}