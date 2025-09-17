package org.dredd.bulletcore.custom_item_manager.registries;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.dredd.bulletcore.custom_item_manager.exceptions.ItemRegisterException;
import org.dredd.bulletcore.models.CustomBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for managing custom items of type {@code T}, where {@code T} extends {@link CustomBase}.
 *
 * @param <T> the type of custom item extending {@link CustomBase}
 * @author dredd
 * @since 1.0.0
 */
public final class ItemRegistry<T extends CustomBase> {

    /**
     * A mapping of {@link CustomBase#customModelData} keys to their corresponding custom item instances.
     */
    private final Int2ObjectMap<T> itemsByModelData;

    /**
     * A mapping of item names to their corresponding custom item instances.
     */
    private final Map<String, T> itemsByName;

    private ItemRegistry() {
        this.itemsByModelData = new Int2ObjectArrayMap<>(16);
        this.itemsByName = new HashMap<>();
    }

    /**
     * Creates a new, empty {@code ItemRegistry} with the default initial parameters.
     */
    static <T extends CustomBase> @NotNull ItemRegistry<T> create() {
        return new ItemRegistry<>();
    }

    /**
     * Retrieves an item by its {@link CustomBase#customModelData} key.
     *
     * @param customModelData custom model data key associated with the item
     * @return the item if found, or {@code null} if not registered
     */
    public @Nullable T getItemOrNull(int customModelData) {
        return itemsByModelData.get(customModelData);
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
    public @NotNull @Unmodifiable Collection<T> getAll() {
        return Collections.unmodifiableCollection(itemsByName.values());
    }

    /**
     * Returns the names of all registered items.
     *
     * @return a collection of all registered item names
     */
    public @NotNull @Unmodifiable Collection<String> getAllNames() {
        return Collections.unmodifiableSet(itemsByName.keySet());
    }

    /**
     * Registers a new item to the registry.
     *
     * @param item the item to register
     * @throws ItemRegisterException if the item is already registered by customModelData or name
     */
    void register(@NotNull T item) throws ItemRegisterException {
        int modelData = item.customModelData;
        String name = item.name;

        T existingByModelData = itemsByModelData.putIfAbsent(modelData, item);
        if (existingByModelData != null)
            throw new ItemRegisterException("Item is already registered with the customModelData: " + modelData);

        T existingByName = itemsByName.putIfAbsent(name, item);
        if (existingByName != null) {
            itemsByModelData.remove(modelData, item);
            throw new ItemRegisterException("Item is already registered with the name: " + name);
        }
    }

    /**
     * Unregisters an item from the registry.
     *
     * @param item the item to unregister
     */
    void unregister(@NotNull T item) {
        itemsByModelData.remove(item.customModelData, item);
        itemsByName.remove(item.name, item);
    }

    /**
     * Clears all registered items from the registry.
     */
    void clearAll() {
        itemsByModelData.clear();
        itemsByName.clear();
    }

    /**
     * Checks if an item exists in the registry by its customModelData.
     *
     * @param customModelData the custom model data of the item
     * @return {@code true} if an item with the given custom model data exists, {@code false} otherwise
     */
    boolean exists(int customModelData) {
        return itemsByModelData.containsKey(customModelData);
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