package org.dredd.bulletcore.models.ammo;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.models.CustomBase;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static org.bukkit.persistence.PersistentDataType.INTEGER;
import static org.dredd.bulletcore.config.messages.translatable.TranslatableMessage.LORE_AMMO_COUNT;

/**
 * Represents ammo items.
 *
 * @author dredd
 * @since 1.0.0
 */
public class Ammo extends CustomBase {

    /**
     * Identifier for ammo count on Ammo ItemStack
     */
    private static final NamespacedKey AMMO_COUNT_KEY = new NamespacedKey("bulletcore", "ammo_count");

    /**
     * The number of ammo units this item can hold.
     */
    public final int maxAmmo;


    /**
     * Constructs a new {@link Ammo} instance.
     * <p>
     * All parameters must be already validated.
     */
    public Ammo(BaseAttributes attrs, int maxAmmo) {
        super(attrs);
        this.maxAmmo = maxAmmo;
    }


    @Override
    public @NotNull ItemStack createItemStack() {
        ItemStack stack = createBaseItemStack();

        setAmmoCount(stack, maxAmmo);
        return stack;
    }

    @Override
    public boolean onRMB(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Right-click with Ammo");
        return false;
    }

    @Override
    public boolean onLMB(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Left-click with Ammo");
        return false;
    }

    @Override
    public boolean onSwapTo(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Swapped to Ammo");
        return false;
    }

    @Override
    public boolean onSwapAway(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Swapped away from Ammo");
        return false;
    }

    /**
     * Retrieves the current ammo count stored in the given {@link ItemStack}'s metadata.
     *
     * @param stack The {@link ItemStack} representing {@link Ammo} to retrieve the ammo count from.
     * @return The number of ammo units currently stored in the ammo stack.<br>
     * Returns {@code 0} if the stack did not store ammo count metadata.
     */
    public int getAmmoCount(@NotNull ItemStack stack) {
        return stack.getItemMeta().getPersistentDataContainer().getOrDefault(AMMO_COUNT_KEY, INTEGER, 0);
    }

    /**
     * Sets the ammo count for the given {@link ItemStack}, updating both persistent data and lore display.
     *
     * @param stack The ammo {@link ItemStack} to modify.
     * @param count The number of ammo units to set for this ammo stack.
     */
    public void setAmmoCount(@NotNull ItemStack stack, int count) {
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(AMMO_COUNT_KEY, INTEGER, count);

        List<Component> lore = meta.lore();
        if (lore != null && !lore.isEmpty()) {
            lore.set(0, LORE_AMMO_COUNT.toTranslatable(Integer.toString(count), Integer.toString(maxAmmo)));
            meta.lore(lore);
            stack.setItemMeta(meta);
        }
    }

    /**
     * Retrieves the current ammo count stored in the given {@link Player}'s inventory.
     *
     * @param player The {@link Player} to retrieve the ammo count from.
     * @return The number of ammo units currently stored in the player's inventory.
     */
    public int getAmmoCount(@NotNull Player player) {
        return Arrays.stream(player.getInventory().getContents())
            .filter(stack -> CustomItemsRegistry.getAmmoOrNull(stack) == this)
            .mapToInt(this::getAmmoCount)
            .sum();
    }

    /**
     * Try to remove the given amount of ammo from the given {@link Player}'s inventory.
     *
     * @param player       The {@link Player} to remove the ammo from.
     * @param removeAmount The number of ammo units to remove from the player's inventory.
     * @return The number of ammo units successfully removed from the player's inventory.
     */
    public int removeAmmo(@NotNull Player player, int removeAmount) {
        if (removeAmount <= 0) return 0;

        int leftToRemove = removeAmount;
        int removed = 0;

        final PlayerInventory inventory = player.getInventory();
        final ItemStack[] contents = inventory.getContents();

        for (int i = 0; i < contents.length; i++) {
            final ItemStack stack = contents[i];
            if (CustomItemsRegistry.getAmmoOrNull(stack) != this) continue;

            int stackAmmoCount = getAmmoCount(stack);

            // Case 1: stackAmmoCount has enough ammo
            if (stackAmmoCount >= leftToRemove) {
                int leftInStack = stackAmmoCount - leftToRemove;

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