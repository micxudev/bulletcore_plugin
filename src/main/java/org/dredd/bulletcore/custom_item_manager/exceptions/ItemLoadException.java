package org.dredd.bulletcore.custom_item_manager.exceptions;

/**
 * Exception thrown when a custom item fails to load properly from configuration or data source.
 *
 * <p>This may occur due to invalid format, missing fields, or type mismatches during deserialization.</p>
 *
 * @author dredd
 * @since 1.0.0
 */
public class ItemLoadException extends Exception {

    /**
     * Constructs a new {@code ItemLoadException} with the specified detail message.
     *
     * @param message the detail message describing the reason for the failure
     */
    public ItemLoadException(String message) {
        super(message);
    }
}