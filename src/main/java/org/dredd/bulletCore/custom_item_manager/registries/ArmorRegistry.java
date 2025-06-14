package org.dredd.bulletCore.custom_item_manager.registries;

import org.dredd.bulletCore.models.armor.Armor;

/**
 * Singleton registry for managing all {@link Armor} items.
 *
 * <p>This class provides centralized access and management for {@code Armor} instances
 * using the underlying {@link ItemRegistry} infrastructure.</p>
 *
 * @author dredd
 * @since 1.0.0
 */
public class ArmorRegistry extends ItemRegistry<Armor> {

    /**
     * The single instance of this registry.
     */
    private static final ArmorRegistry INSTANCE = new ArmorRegistry();

    /**
     * Private constructor to enforce singleton usage.
     */
    private ArmorRegistry() {}

    /**
     * Returns the singleton instance of {@code ArmorRegistry}.
     *
     * @return the singleton instance
     */
    static ArmorRegistry getInstance() {
        return INSTANCE;
    }
}