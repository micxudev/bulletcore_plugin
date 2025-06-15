package org.dredd.bulletcore.custom_item_manager;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dredd.bulletcore.custom_item_manager.exceptions.ItemLoadException;
import org.dredd.bulletcore.models.CustomBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a unique key for registering and identifying custom items (classes that extend {@link CustomBase }).
 *
 * @author dredd
 * @since 1.0.0
 */
public final class MaterialStorage {

    /**
     * Internal registry of all created {@link MaterialStorage} instances.
     * <p>
     * Ensures uniqueness based on {@link #material} and {@link #customModelData} pairs.
     * <p>
     * Supports reverse lookup via the {@link #getFromItem(ItemStack)} method.
     * <p>
     * Populated exclusively via the {@link #create(String, int)} method.
     */
    private static final Map<Key, MaterialStorage> STORAGE = new ConcurrentHashMap<>();

    /**
     * The base Minecraft {@link Material} used for the custom item.<br>
     * This affects the item's appearance if no resource pack is used.
     */
    public final @NotNull Material material;

    /**
     * This is the value used by resource packs to apply the correct model.<br>
     *
     * @see ItemMeta#getCustomModelData()
     */
    public final int customModelData;

    private MaterialStorage(@NotNull Material material, int customModelData) {
        this.material = material;
        this.customModelData = customModelData;
    }

    /**
     * Parses the given material name and returns the Material if it supports {@link ItemMeta}.
     *
     * @param materialName The name of the Material (e.g., "DIAMOND_SWORD")
     * @return Material that supports ItemMeta
     * @throws ItemLoadException if the material is invalid or does not support ItemMeta
     */
    private static @NotNull Material getMetaCapableMaterial(@Nullable String materialName)
        throws ItemLoadException {
        if (materialName == null || materialName.isBlank())
            throw new ItemLoadException("Material name cannot be null or blank");

        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            throw new ItemLoadException("Invalid material name: " + materialName);
        }

        if (!material.isItem())
            throw new ItemLoadException("Material is not an item: " + material);

        if (new ItemStack(material).getItemMeta() == null)
            throw new ItemLoadException("Material does not support ItemMeta: " + material);

        return material;
    }

    /**
     * Creates a new {@link MaterialStorage} instance for the given material and customModelData<br>
     * or throws an exception.
     *
     * @param materialName    The name of the Material (e.g., "CROSSBOW")
     * @param customModelData Used to distinguish this item in resource packs
     * @return A new {@link MaterialStorage} instance
     * @throws ItemLoadException If a MaterialStorage with the same material and CMD already exists
     */
    public static @NotNull MaterialStorage create(@Nullable String materialName, int customModelData)
        throws ItemLoadException {

        Material material = getMetaCapableMaterial(materialName);
        Key key = new Key(material, customModelData);
        MaterialStorage newStorage = new MaterialStorage(material, customModelData);

        MaterialStorage existing = STORAGE.putIfAbsent(key, newStorage);

        if (existing != null)
            throw new ItemLoadException(
                "Duplicate MaterialStorage: " + material + " with CustomModelData " + customModelData
            );

        return newStorage;
    }

    /**
     * Attempts to look up a registered {@link MaterialStorage} that matches the given {@link ItemStack}.
     *
     * @param stack The item to check
     * @return The matching {@link MaterialStorage}, or {@code null} if none is registered
     */
    public static @Nullable MaterialStorage getFromItem(@Nullable ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasCustomModelData()) return null;
        return STORAGE.get(new Key(stack.getType(), meta.getCustomModelData()));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MaterialStorage that)) return false;
        return customModelData == that.customModelData && material == that.material;
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, customModelData);
    }

    @Override
    public String toString() {
        return "MaterialStorage{" +
            "material=" + material +
            ", customModelData=" + customModelData +
            '}';
    }

    /**
     * Key used for internal indexing of MaterialStorage instances.<br>
     * Combines {@link #material} and {@link #customModelData } as a unique identifier.
     */
    private record Key(Material material, int customModelData) {}
}