package org.dredd.bulletcore.custom_item_manager.exceptions;

/**
 * This exception is thrown when a custom item failed to register in its respective registry.
 *
 * @author dredd
 * @since 1.0.0
 */
public class ItemRegisterException extends Exception {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message explaining the registration error
     */
    public ItemRegisterException(String message) {
        super(message);
    }
}