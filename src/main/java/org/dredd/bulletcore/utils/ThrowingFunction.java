package org.dredd.bulletcore.utils;

/**
 * A functional interface representing a function that takes an input and returns a result<br>
 * but is allowed to throw a checked {@link Exception}.
 *
 * @param <T> the input type
 * @param <R> the return type
 * @since 1.0.0
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> {

    /**
     * Applies this function to the given input.
     *
     * @param t the input value
     * @return the function result
     * @throws Exception if an exception occurs during processing
     */
    R apply(T t) throws Exception;
}