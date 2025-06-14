package org.dredd.bulletCore.custom_item_manager.registries;

import org.dredd.bulletCore.models.grenades.Grenade;

/**
 * Singleton registry for managing all {@link Grenade} items.
 *
 * <p>This class provides centralized access and management for {@code Grenade} instances
 * using the underlying {@link ItemRegistry} infrastructure.</p>
 *
 * @author dredd
 * @since 1.0.0
 */
public class GrenadeRegistry extends ItemRegistry<Grenade> {

    /**
     * The single instance of this registry.
     */
    private static final GrenadeRegistry INSTANCE = new GrenadeRegistry();

    /**
     * Private constructor to enforce singleton usage.
     */
    private GrenadeRegistry() {}

    /**
     * Returns the singleton instance of {@code GrenadeRegistry}.
     *
     * @return the singleton instance
     */
    static GrenadeRegistry getInstance() {
        return INSTANCE;
    }
}