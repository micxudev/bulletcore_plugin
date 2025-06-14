package org.dredd.bulletCore.models;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dredd.bulletCore.custom_item_manager.MaterialStorage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The base class for all custom models.
 *
 * @author dredd
 * @since 1.0.0
 */
public abstract class CustomBase {

    // ----- Fields -----
    /**
     * The internal, unique identifier for the item used in commands (e.g., `give {@code ak47}`).<br>
     * It's recommended to keep this lowercase and without space.
     */
    public final String name;

    /**
     * Uniquely identifies the in-game item associated with this custom object.<br>
     * Used for item recognition, registration, and matching during runtime.
     */
    public final MaterialStorage materialStorage;

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


    // ----- Constructor -----
    /**
     * Constructs a new {@link CustomBase} instance.
     *
     * @param attrs the {@link BaseAttributes} to use for this custom object
     */
    protected CustomBase(BaseAttributes attrs) {
        this.name = attrs.name;
        this.materialStorage = attrs.materialStorage;
        this.displayName = attrs.displayName;
        this.lore = attrs.lore;
        this.maxStackSize = attrs.maxStackSize;
    }


    // ----- Methods -----
    /**
     * Creates a new {@link ItemStack} with all the {@link BaseAttributes} already set.
     *
     * @return A new {@link ItemStack} with the base attributes applied
     */
    protected @NotNull ItemStack createBaseItemStack() {
        ItemStack itemStack = new ItemStack(this.materialStorage.material);
        ItemMeta meta = itemStack.getItemMeta();

        meta.setCustomModelData(this.materialStorage.customModelData);
        meta.displayName(this.displayName);
        meta.lore(this.lore);
        meta.setMaxStackSize(this.maxStackSize);

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

    /*public abstract boolean onRMB(Player player, ItemStack item);

    public abstract boolean onLMB(Player player, ItemStack item);

    public abstract boolean onSwapTo(Player player, ItemStack item);

    public abstract boolean onSwapAway(Player player, ItemStack item);*/


    // ----- Additional Classes -----
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
        MaterialStorage materialStorage,
        Component displayName,
        List<Component> lore,
        int maxStackSize
    ) {}
}