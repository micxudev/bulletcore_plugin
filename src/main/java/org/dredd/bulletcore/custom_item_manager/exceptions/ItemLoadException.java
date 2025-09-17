package org.dredd.bulletcore.custom_item_manager.exceptions;

/**
 * This exception is thrown when a custom item failed to load from a configuration file.
 *
 * @author dredd
 * @since 1.0.0
 */
public class ItemLoadException extends Exception {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message describing the reason for the failure
     */
    public ItemLoadException(String message) {
        super(message);
    }
}