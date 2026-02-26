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
     * Linearly interpolates between two values.
     * <p>
     * The interpolation factor {@code t} is <strong>NOT clamped</strong>.
     * <br>
     * This means values of {@code t} outside the range {@code [0, 1]}
     * will extrapolate beyond {@code a} and {@code b}.
     *
     * @param a The start value
     * @param b The end value
     * @param t The interpolation factor
     * @return The interpolated (or extrapolated) value
     */
    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /**
     * Returns the mathematical floor of the given value as an {@code int}.
     * <p>
     * The result is the largest integer less than or equal to {@code a}.
     *
     * @param a The input value
     * @return The floor of {@code a}
     */
    public static int floor(double a) {
        final int floor = (int) a;
        return (a < floor) ? floor - 1 : floor;
    }

    /**
     * Returns the mathematical floor of the given value as a {@code long}.
     * <p>
     * The result is the largest long less than or equal to {@code a}.
     *
     * @param a The input value
     * @return The floor of {@code a}
     */
    public static long lfloor(double a) {
        final long floor = (long) a;
        return (a < floor) ? floor - 1L : floor;
    }

    /**
     * Returns the sign of the given value.
     * <ul>
     *     <li>{@code 0} if the value is zero</li>
     *     <li>{@code 1} if the value is positive</li>
     *     <li>{@code -1} if the value is negative</li>
     * </ul>
     * @param a The input value
     * @return The sign of {@code a}
     */
    public static int sign(double a) {
        if (a == 0.0D) return 0;
        return (a > 0.0D) ? 1 : -1;
    }

    /**
     * Returns the fractional part of the given value.
     * <p>
     * The result is always in the range {@code [0, 1)} for finite inputs.
     * <p>
     * For negative values, the fractional part is still positive:
     * {@code frac(-3.7) == 0.3}.
     *
     * @param a The input value
     * @return The fractional part of {@code a}
     */
    public static double frac(double a) {
        return a - lfloor(a);
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