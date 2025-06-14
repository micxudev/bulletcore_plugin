package org.dredd.bulletCore.custom_item_manager.exceptions;

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
     * Constructs a new {@code ItemRegisterException} with the specified detail message.
     *
     * @param message the detail message explaining the registration error
     */
    public ItemRegisterException(String message) {
        super(message);
    }
}