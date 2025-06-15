package org.dredd.bulletcore.custom_item_manager.registries;

import org.dredd.bulletcore.models.weapons.Weapon;

/**
 * Singleton registry for managing all {@link Weapon} items.
 *
 * <p>This class provides centralized access and management for {@code Weapon} instances
 * using the underlying {@link ItemRegistry} infrastructure.</p>
 *
 * @author dredd
 * @since 1.0.0
 */
public class WeaponRegistry extends ItemRegistry<Weapon> {

    /**
     * The single instance of this registry.
     */
    private static final WeaponRegistry INSTANCE = new WeaponRegistry();

    /**
     * Private constructor to enforce singleton usage.
     */
    private WeaponRegistry() {}

    /**
     * Returns the singleton instance of {@code WeaponRegistry}.
     *
     * @return the singleton instance
     */
    static WeaponRegistry getInstance() {
        return INSTANCE;
    }
}