package org.dredd.bulletCore.custom_item_manager.registries;

import org.dredd.bulletCore.custom_item_manager.MaterialStorage;
import org.dredd.bulletCore.models.CustomBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract registry for managing custom items of type {@code T}, where {@code T} extends {@link CustomBase}.
 *
 * @param <T> the type of custom item extending {@link CustomBase}
 * @author dredd
 * @since 1.0.0
 */
abstract class ItemRegistry<T extends CustomBase> {

    /**
     * A mapping of {@link MaterialStorage} keys to their corresponding custom item instances.
     */
    private final Map<MaterialStorage, T> items = new HashMap<>();

    /**
     * A mapping of item names to their corresponding custom item instances.
     */
    private final Map<String, T> itemsByName = new HashMap<>();

    /**
     * Retrieves an item by its {@link MaterialStorage} key.
     *
     * @param storage the material storage key associated with the item
     * @return the item if found, or {@code null} if not registered
     */
    public @Nullable T getItemOrNull(MaterialStorage storage) {
        return items.get(storage);
    }

    /**
     * Retrieves an item by its name.
     *
     * @param name the name associated with the item
     * @return the item if found, or {@code null} if not registered
     */
    public @Nullable T getItemOrNull(String name) {
        return itemsByName.get(name);
    }

    /**
     * Returns all registered items.
     *
     * @return a collection of all registered custom items
     */
    public @NotNull Collection<T> getAll() {
        return items.values();
    }

    /**
     * Returns the names of all registered items.
     *
     * @return a collection of all registered item names
     */
    public @NotNull Collection<String> getAllNames() {
        return itemsByName.keySet();
    }

    /**
     * Registers a new item to the registry.
     *
     * @param item the item to register
     */
    void register(T item) {
        items.put(item.materialStorage, item);
        itemsByName.put(item.name, item);
    }

    /**
     * Clears all registered items from the registry.
     */
    void clearAll() {
        items.clear();
        itemsByName.clear();
    }

    /**
     * Checks if an item exists in the registry by its name.
     *
     * @param name the name of the item
     * @return {@code true} if an item with the given name exists, {@code false} otherwise
     */
    boolean exists(String name) {
        return itemsByName.containsKey(name);
    }
}