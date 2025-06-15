package org.dredd.bulletcore.custom_item_manager.exceptions;

import java.io.Serial;

/**
 * Exception thrown when a custom item fails to load properly from a configuration or data source.
 *
 * <p>This may occur due to invalid format, missing fields, or type mismatches during deserialization.</p>
 *
 * @author dredd
 * @since 1.0.0
 */
public class ItemLoadException extends Exception {

    /**
     * Used during deserialization to verify that the sender and receiver of a serialized object
     * have loaded classes for that object that are compatible with respect to serialization.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ItemLoadException} with the specified detail message.
     *
     * @param message the detail message describing the reason for the failure
     */
    public ItemLoadException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code ItemLoadException} with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of this exception
     */
    public ItemLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code ItemLoadException} with the specified cause.
     *
     * @param cause the cause of this exception
     */
    public ItemLoadException(Throwable cause) {
        super(cause);
    }
}