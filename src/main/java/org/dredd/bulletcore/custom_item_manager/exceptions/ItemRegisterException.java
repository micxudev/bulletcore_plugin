package org.dredd.bulletcore.custom_item_manager.exceptions;

import java.io.Serial;

/**
 * Exception thrown when a custom item fails to register in its respective registry.
 *
 * <p>Common causes include null items, unknown item types, or naming conflicts.</p>
 *
 * @author dredd
 * @since 1.0.0
 */
public class ItemRegisterException extends Exception {

    /**
     * Used during deserialization to verify that the sender and receiver of a serialized object
     * have loaded classes for that object that are compatible with respect to serialization.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ItemRegisterException} with the specified detail message.
     *
     * @param message the detail message explaining the registration error
     */
    public ItemRegisterException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code ItemRegisterException} with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of this exception
     */
    public ItemRegisterException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code ItemRegisterException} with the specified cause.
     *
     * @param cause the cause of this exception
     */
    public ItemRegisterException(Throwable cause) {
        super(cause);
    }
}