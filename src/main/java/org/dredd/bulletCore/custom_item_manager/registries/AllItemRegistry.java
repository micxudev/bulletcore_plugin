package org.dredd.bulletCore.custom_item_manager.registries;

import org.dredd.bulletCore.models.CustomBase;

/**
 * Singleton registry for managing all generic {@link CustomBase} items, regardless of type.
 *
 * <p>This class serves as a unified registry for all item categories.
 * It is useful when a more general lookup or cross-type operation is needed.</p>
 *
 * @author dredd
 * @since 1.0.0
 */
public final class AllItemRegistry extends ItemRegistry<CustomBase> {

    /**
     * The single instance of this registry.
     */
    private static final AllItemRegistry INSTANCE = new AllItemRegistry();

    /**
     * Private constructor to enforce singleton usage.
     */
    private AllItemRegistry() {}

    /**
     * Returns the singleton instance of {@code AllItemRegistry}.
     *
     * @return the singleton instance
     */
    static AllItemRegistry getInstance() {
        return INSTANCE;
    }
}