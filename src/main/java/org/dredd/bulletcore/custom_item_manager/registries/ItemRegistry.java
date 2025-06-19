package org.dredd.bulletcore.custom_item_manager.registries;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.dredd.bulletcore.custom_item_manager.exceptions.ItemRegisterException;
import org.dredd.bulletcore.models.CustomBase;
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
     * A mapping of {@link CustomBase#customModelData} keys to their corresponding custom item instances.
     */
    private final Int2ObjectMap<T> items = new Int2ObjectArrayMap<>(16);

    /**
     * A mapping of item names to their corresponding custom item instances.
     */
    private final Map<String, T> itemsByName = new HashMap<>();

    /**
     * Retrieves an item by its {@link CustomBase#customModelData} key.
     *
     * @param customModelData custom model data key associated with the item
     * @return the item if found, or {@code null} if not registered
     */
    public @Nullable T getItemOrNull(int customModelData) {
        return items.get(customModelData);
    }

    /**
     * Retrieves an item by its name.
     *
     * @param name the name associated with the item
     * @return the item if found, or {@code null} if not registered
     */
    public @Nullable T getItemOrNull(@NotNull String name) {
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
    void register(@NotNull T item) throws ItemRegisterException {
        int key1 = item.customModelData;
        String key2 = item.name;

        T existingByModelData = items.putIfAbsent(key1, item);
        if (existingByModelData != null)
            throw new ItemRegisterException("Item with customModelData " + key1 + " already registered");

        T existingByName = itemsByName.putIfAbsent(key2, item);
        if (existingByName != null) {
            items.remove(key1, item);
            throw new ItemRegisterException("Item already registered by name: " + key2);
        }
    }

    /**
     * Clears all registered items from the registry.
     */
    void clearAll() {
        items.clear();
        itemsByName.clear();
    }

    /**
     * Checks if an item exists in the registry by its customModelData.
     *
     * @param customModelData the custom model data of the item
     * @return {@code true} if an item with the given custom model data exists, {@code false} otherwise
     */
    boolean exists(int customModelData) {
        return items.containsKey(customModelData);
    }

    /**
     * Checks if an item exists in the registry by its name.
     *
     * @param name the name of the item
     * @return {@code true} if an item with the given name exists, {@code false} otherwise
     */
    boolean exists(@NotNull String name) {
        return itemsByName.containsKey(name);
    }
}