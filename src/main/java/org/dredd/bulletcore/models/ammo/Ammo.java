package org.dredd.bulletcore.models.ammo;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.models.CustomBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.bukkit.persistence.PersistentDataType.INTEGER;
import static org.dredd.bulletcore.config.messages.TranslatableMessages.LORE_AMMO_COUNT;
import static org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry.isAmmo;

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
     * @return The number of ammo units currently stored in the ammo.<br>
     * Returns {@code 0} if the item is not ammo or null.
     */
    public static int getAmmoCount(@Nullable ItemStack stack) {
        if (!isAmmo(stack)) return 0;
        return stack.getItemMeta().getPersistentDataContainer().getOrDefault(AMMO_COUNT_KEY, INTEGER, 0);
    }

    /**
     * Sets the ammo count for the given {@link ItemStack}, updating both persistent data and lore display.
     * <p>
     * If the item is not registered ammo, no changes will be made.</p>
     *
     * @param stack The {@link ItemStack} to modify.
     * @param count The number of ammo units to set for this ammo.
     */
    public static void setAmmoCount(@Nullable ItemStack stack, int count) {
        Ammo ammo = CustomItemsRegistry.getAmmoOrNull(stack);
        if (ammo == null) return;

        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(AMMO_COUNT_KEY, INTEGER, count);

        List<Component> lore = meta.lore();
        lore.set(0, LORE_AMMO_COUNT.of(count, ammo.maxAmmo));
        meta.lore(lore);
        stack.setItemMeta(meta);
    }
}