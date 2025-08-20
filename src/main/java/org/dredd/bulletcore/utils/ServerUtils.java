package org.dredd.bulletcore.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for retrieving information about the current server state.
 *
 * @since 1.0.0
 */
public final class ServerUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private ServerUtils() {}

    /**
     * An empty immutable list.
     */
    public static final List<String> EMPTY_LIST = Collections.emptyList();

    /**
     * @return a list of player names currently online
     */
    public static List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .toList();
    }

    /**
     * Creates an ItemStack with the specified material and CustomModelData.
     *
     * @param material        the item material (must support metadata)
     * @param customModelData the custom model data value
     * @return a new ItemStack with the given model data
     * @throws IllegalStateException if the material doesn't support ItemMeta
     */
    public static @NotNull ItemStack createCustomModelItem(@NotNull Material material, int customModelData) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            throw new IllegalStateException("Material " + material + " does not support ItemMeta.");
        meta.setCustomModelData(customModelData);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Generates a random {@code bulletcore} namespaced key.
     *
     * @return a new {@link NamespacedKey} with a random UUID as its value.
     */
    public static @NotNull NamespacedKey rndNamespacedKey() {
        return new NamespacedKey("bulletcore", UUID.randomUUID().toString());
    }
}