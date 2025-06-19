package org.dredd.bulletcore.models;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The base class for all custom models.
 *
 * @author dredd
 * @since 1.0.0
 */
public abstract class CustomBase {

    /**
     * The internal, unique identifier for the item used in commands (e.g., `give {@code ak47}`).<br>
     * It's recommended to keep this lowercase and without space.
     */
    public final String name;

    /**
     * This is the value used by resource packs to apply the correct model.<br>
     * Used by the plugin to uniquely identify the in-game item associated with this custom object.
     */
    public final int customModelData;

    /**
     * The base Minecraft {@link Material} used for the custom item.<br>
     * This affects the item's appearance if no resource pack is used.
     */
    public final Material material;

    /**
     * The name shown on the item in-game. Supports
     * <a href="https://docs.adventure.kyori.net/minimessage.html">MiniMessage format</a>.
     */
    public final Component displayName;

    /**
     * A list of {@link Component} that will appear as the item's lore (description below the name).
     */
    public final List<Component> lore;

    /**
     * This is the maximum amount that an item will stack, must be between {@code 1} and {@code 99} (inclusive).<br>
     * Bounds are defined by the {@link ItemMeta#setMaxStackSize(Integer)} method.
     */
    public final int maxStackSize;


    /**
     * Constructs a new {@link CustomBase} instance.
     *
     * @param attrs the {@link BaseAttributes} to use for this custom object
     */
    protected CustomBase(BaseAttributes attrs) {
        this.name = attrs.name;
        this.customModelData = attrs.customModelData;
        this.material = attrs.material;
        this.displayName = attrs.displayName;
        this.lore = attrs.lore;
        this.maxStackSize = attrs.maxStackSize;
    }


    /**
     * Creates a new {@link ItemStack} with all the {@link BaseAttributes} already set.
     *
     * @return A new {@link ItemStack} with the base attributes applied
     */
    protected @NotNull ItemStack createBaseItemStack() {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();

        meta.setCustomModelData(customModelData);
        meta.displayName(displayName);
        meta.lore(lore);
        meta.setMaxStackSize(maxStackSize);

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * Subclasses must implement this method to provide additional information to the final item stack.<br>
     *
     * @return A new {@link ItemStack} with all the necessary attributes and additional ones applied.
     * @see #createBaseItemStack()
     */
    public abstract @NotNull ItemStack createItemStack();

    /**
     * Called when the player right-clicks the with custom item in hand.
     *
     * @param player   The player who right-clicked
     * @param usedItem The item that was right-clicked
     * @return {@code true} if the involved event should be canceled, {@code false} otherwise
     */
    public abstract boolean onRMB(@NotNull Player player, @NotNull ItemStack usedItem);

    /**
     * Called when the player left-clicks the with custom item in hand.
     *
     * @param player   The player who left-clicked
     * @param usedItem The item that was left-clicked
     * @return {@code true} if the involved event should be canceled, {@code false} otherwise
     */
    public abstract boolean onLMB(@NotNull Player player, @NotNull ItemStack usedItem);

    /*public abstract boolean onSwapTo(Player player, ItemStack item);

    public abstract boolean onSwapAway(Player player, ItemStack item);*/


    /**
     * A compact, immutable holder for base-level properties shared by all custom items.
     * <p>
     * This record is used to load and transfer shared attributes between the
     * configuration loader and all classes that extend {@link CustomBase}.
     * </p>
     *
     * <h4>Why use BaseAttributes?</h4>
     * <ul>
     *   <li> Avoids duplicated code for common field loading in multiple subclasses</li>
     *   <li> Centralizes default values and parsing logic for base fields</li>
     *   <li> Enables safe and immutable passing of base data using Java {@code record}</li>
     *   <li> Makes the base field set extendable without touching subclasses</li>
     * </ul>
     */
    public record BaseAttributes(
        String name,
        int customModelData,
        Material material,
        Component displayName,
        List<Component> lore,
        int maxStackSize
    ) {}
}