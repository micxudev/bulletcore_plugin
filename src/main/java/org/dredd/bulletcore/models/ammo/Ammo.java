package org.dredd.bulletcore.models.ammo;

import java.util.Arrays;
import java.util.List;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.dredd.bulletcore.custom_item_manager.exceptions.ItemLoadException;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.models.CustomBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.bukkit.persistence.PersistentDataType.INTEGER;
import static org.dredd.bulletcore.config.messages.translatable.TranslatableMessage.LORE_AMMO_COUNT;

/**
 * Represents ammo items.
 *
 * @author dredd
 * @since 1.0.0
 */
public class Ammo extends CustomBase {

    // ----------< Static >----------

    /**
     * Identifier for ammo count on Ammo ItemStack
     */
    private static final NamespacedKey AMMO_COUNT_KEY = new NamespacedKey("bulletcore", "ammo_count");


    // ----------< Instance >----------

    // -----< Attributes >-----

    /**
     * The number of ammo units this ammo item can hold.
     */
    public final int maxAmmo;

    /**
     * String representation of {@link #maxAmmo}
     */
    public final String maxAmmoString;

    // -----< Construction >-----

    /**
     * Loads and validates an ammo item definition from the given config.
     *
     * @param config the YAML configuration source
     * @throws ItemLoadException if validation fails
     */
    public Ammo(@NotNull YamlConfiguration config) throws ItemLoadException {
        super(config);

        this.maxAmmo = Math.clamp(config.getInt("maxAmmo", 100), 1, Integer.MAX_VALUE);
        this.maxAmmoString = Integer.toString(maxAmmo);

        super.lore.add(0, Component.empty()); // Ammo count will be here on ItemStack creation
    }

    // -----< Ammo Behavior >-----

    @Override
    protected void applyCustomAttributes(@NotNull ItemStack stack) {
        setAmmoCount(stack, maxAmmo);
    }

    // -----< ItemStack | Ammo >-----

    /**
     * Retrieves the current ammo count stored in the given {@link ItemStack}'s metadata.
     *
     * @param stack the stack representing ammo to retrieve the ammo count from
     * @return the number of ammo units currently stored in the ammo stack or
     * {@code 0} if the stack did not store ammo count metadata.
     */
    public int getAmmoCount(@NotNull ItemStack stack) {
        final Integer value = stack.getItemMeta().getPersistentDataContainer().get(AMMO_COUNT_KEY, INTEGER);
        return value == null ? 0 : Math.max(0, value);
    }

    /**
     * Sets the ammo count for the given {@link ItemStack}, updating both persistent data and lore display.
     *
     * @param stack the ammo stack to modify
     * @param count the number of ammo units to set for this ammo stack
     */
    public void setAmmoCount(@NotNull ItemStack stack,
                             int count) {
        final ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(AMMO_COUNT_KEY, INTEGER, count);

        final List<Component> lore = meta.lore();
        if (lore != null && !lore.isEmpty()) {
            lore.set(0, LORE_AMMO_COUNT.toTranslatable(Integer.toString(count), maxAmmoString));
            meta.lore(lore);
            stack.setItemMeta(meta);
        }
    }

    /**
     * Determines whether the given {@link ItemStack} represents this specific type of ammo.
     *
     * @param stack the stack to check
     * @return {@code true} if the given stack corresponds to this ammo type, {@code false} otherwise.
     */
    public boolean isThisAmmo(@Nullable ItemStack stack) {
        return CustomItemsRegistry.getAmmoOrNull(stack) == this;
    }

    // -----< Player | Ammo >-----

    /**
     * Determines whether the given {@link Player} has at least one this ammo item in their inventory.
     *
     * @param player the player to check
     * @return {@code true} if the player has at least one this ammo item in their inventory, {@code false} otherwise.
     */
    public boolean hasAmmo(@NotNull Player player) {
        return Arrays.stream(player.getInventory().getContents())
            .anyMatch(this::isThisAmmo);
    }

    /**
     * Retrieves the current ammo count stored in the given {@link Player}'s inventory.
     *
     * @param player the player to retrieve the ammo count from
     * @return the number of ammo units currently stored in the player's inventory.
     */
    public int getAmmoCount(@NotNull Player player) {
        return Arrays.stream(player.getInventory().getContents())
            .filter(this::isThisAmmo)
            .mapToInt(this::getAmmoCount)
            .sum();
    }

    /**
     * Tries to remove the given amount of ammo from the given {@link Player}'s inventory.
     *
     * @param player       the player to remove the ammo from
     * @param removeAmount the number of ammo units to remove from the player's inventory
     * @return the number of ammo units successfully removed from the player's inventory.
     */
    public int removeAmmo(@NotNull Player player,
                          int removeAmount) {
        if (removeAmount <= 0) return 0;

        int leftToRemove = removeAmount;
        int removed = 0;

        final PlayerInventory inventory = player.getInventory();
        final ItemStack[] contents = inventory.getContents();

        for (int i = 0; i < contents.length; i++) {
            final ItemStack stack = contents[i];
            if (!isThisAmmo(stack)) continue;

            final int stackAmmoCount = getAmmoCount(stack);

            // Case 1: stackAmmoCount has enough ammo
            if (stackAmmoCount >= leftToRemove) {
                final int leftInStack = stackAmmoCount - leftToRemove;

                if (leftInStack <= 0) {
                    inventory.setItem(i, null);
                } else {
                    setAmmoCount(stack, leftInStack);
                }

                return removeAmount;
            }

            // Case 2: stackAmmoCount has not enough ammo
            removed += stackAmmoCount;
            leftToRemove -= stackAmmoCount;
            inventory.setItem(i, null);
        }

        return removed;
    }
}