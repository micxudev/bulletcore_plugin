package org.dredd.bulletCore.custom_item_manager.registries;

import org.dredd.bulletCore.models.ammo.Ammo;

/**
 * Singleton registry for managing all {@link Ammo} items.
 *
 * <p>This class provides centralized access and management for {@code Ammo} instances
 * using the underlying {@link ItemRegistry} infrastructure.</p>
 *
 * @author dredd
 * @since 1.0.0
 */
public class AmmoRegistry extends ItemRegistry<Ammo> {

    /**
     * The single instance of this registry.
     */
    private static final AmmoRegistry INSTANCE = new AmmoRegistry();

    /**
     * Private constructor to enforce singleton usage.
     */
    private AmmoRegistry() {}

    /**
     * Returns the singleton instance of {@code AmmoRegistry}.
     *
     * @return the singleton instance
     */
    static AmmoRegistry getInstance() {
        return INSTANCE;
    }
}