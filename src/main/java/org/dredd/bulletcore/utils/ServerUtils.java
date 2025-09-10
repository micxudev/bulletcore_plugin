package org.dredd.bulletcore.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

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
     * A list containing a single arrow item used to charge crossbows.
     */
    private static final List<ItemStack> CHARGED_PROJECTILES_LIST = Collections.singletonList(new ItemStack(Material.ARROW));

    /**
     * @return a list of player names currently online
     */
    public static @NotNull @Unmodifiable List<String> getOnlinePlayerNames() {
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

    /**
     * Charges or discharges a crossbow if the given stack has CrossbowMeta.
     *
     * @param stack  the item stack; can be null
     * @param charge true to charge, false to discharge
     */
    public static void chargeOrDischargeIfCrossbowMeta(@Nullable ItemStack stack, boolean charge) {
        if (stack != null && stack.getItemMeta() instanceof CrossbowMeta meta) {
            meta.setChargedProjectiles(charge ? CHARGED_PROJECTILES_LIST : null);
            stack.setItemMeta(meta);
        }
    }

    /**
     * Charges a crossbow if the given stack has CrossbowMeta.
     *
     * @param stack the item stack; can be null
     */
    public static void chargeIfCrossbowMeta(@Nullable ItemStack stack) {
        chargeOrDischargeIfCrossbowMeta(stack, true);
    }

    /**
     * Discharges a crossbow if the given stack has CrossbowMeta.
     *
     * @param stack the item stack; can be null
     */
    public static void dischargeIfCrossbowMeta(@Nullable ItemStack stack) {
        chargeOrDischargeIfCrossbowMeta(stack, false);
    }

    /**
     * Charges or discharges a weapon if it is a crossbow.
     *
     * @param stack  the item stack; can be null
     * @param charge true to charge, false to discharge
     */
    public static void chargeOrDischargeIfWeapon(@Nullable ItemStack stack, boolean charge) {
        if (CustomItemsRegistry.isWeapon(stack)) chargeOrDischargeIfCrossbowMeta(stack, charge);
    }

    /**
     * Charges a weapon if it is a crossbow.
     *
     * @param stack the item stack; can be null
     */
    public static void chargeIfWeapon(@Nullable ItemStack stack) {
        if (CustomItemsRegistry.isWeapon(stack)) chargeIfCrossbowMeta(stack);
    }

    /**
     * Discharges a weapon if it is a crossbow.
     *
     * @param stack the item stack; can be null
     */
    public static void dischargeIfWeapon(@Nullable ItemStack stack) {
        if (CustomItemsRegistry.isWeapon(stack)) dischargeIfCrossbowMeta(stack);
    }
}