package org.dredd.bulletcore.models;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dredd.bulletcore.custom_item_manager.exceptions.ItemLoadException;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.utils.ComponentUtils;
import org.dredd.bulletcore.utils.ServerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The base class for all custom items.
 *
 * @author dredd
 * @since 1.0.0
 */
public abstract class CustomBase {

    // -----< Attributes >-----

    /**
     * Lazily initialized {@link ItemStack} representing this custom item.<br>
     * Created on the first request and cloned for faster subsequent creation.
     */
    private volatile @Nullable ItemStack prototype;

    /**
     * The internal, unique identifier for the item used in commands (e.g., `give {@code ak47}`).
     */
    public final String name;

    /**
     * The value used by resource packs to apply the model.<br>
     * Used by the plugin to uniquely identify the in-game item associated with this custom item.
     */
    public final int customModelData;

    /**
     * The base Minecraft {@link Material} used for this custom item.<br>
     * This affects the item's appearance if no resource pack is used.
     */
    public final Material material;

    /**
     * The name shown on the item stack in-game. Supports
     * <a href="https://docs.adventure.kyori.net/minimessage.html">MiniMessage format</a>.
     */
    public final Component displayName;

    /**
     * A string representation of the {@link #displayName} field with all styles being removed.
     */
    public final String displayNameString;

    /**
     * A list of {@link Component} that will appear as the item's lore (description below the name).
     */
    public final List<Component> lore;

    /**
     * The maximum amount that an item will stack,
     * must be between {@code 1} and {@code 99} (inclusive).
     */
    public final int maxStackSize;

    // -----< Construction >-----

    /**
     * Loads and validates a custom item base definition from the given config.
     *
     * @param config the YAML configuration source
     * @throws ItemLoadException if validation fails
     */
    protected CustomBase(@NotNull YamlConfiguration config) throws ItemLoadException {
        this.name = config.getString("name", null);
        if (!CustomItemsRegistry.isValidName(name))
            throw new ItemLoadException("Name: '" + name + "' does not match " + CustomItemsRegistry.VALID_NAME.pattern());

        this.customModelData = config.getInt("customModelData", 0);
        if (!CustomItemsRegistry.isValidCustomModelData(customModelData))
            throw new ItemLoadException("CustomModelData: " + customModelData + " is negative or does not end with 2 zeroes");

        this.material = ServerUtils.getMetaCapableMaterial(config.getString("material", null));

        this.displayName = config.getRichMessage("displayName", ComponentUtils.plainWhite(name));

        this.displayNameString = PlainTextComponentSerializer.plainText().serialize(displayName);

        final List<String> loreList = config.getStringList("lore");
        if (loreList.size() > 256)
            throw new ItemLoadException("Lore cannot have more than 256 lines");

        this.lore = loreList.stream()
            .map(ComponentUtils::deserialize)
            .collect(Collectors.toList());

        this.maxStackSize = Math.clamp(config.getInt("maxStackSize", material.getMaxStackSize()), 1, 99);
    }

    // -----< Item Behavior >-----

    /**
     * Creates and returns a clone of this custom item's prototype stack.
     * <p>
     * Lazily initializes the prototype if it hasn't been created yet.
     *
     * @return a new stack representing this custom item
     */
    public @NotNull ItemStack createItemStack() {
        if (prototype == null) {
            synchronized (this) {
                if (prototype == null)
                    prototype = createPrototype();
            }
        }
        return prototype.clone();
    }

    /**
     * Allows subclasses to add or modify attributes on the base item stack.
     *
     * @param stack the base stack to customize
     */
    protected abstract void applyCustomAttributes(@NotNull ItemStack stack);

    /**
     * Called when a player right-clicks with this custom item in the main hand.
     *
     * @param player the player who right-clicked
     * @param stack  the item stack that was right-clicked with
     * @return {@code true} if the involved event should be canceled, {@code false} otherwise
     */
    public abstract boolean onRMB(@NotNull Player player,
                                  @NotNull ItemStack stack);

    /**
     * Called when a player left-clicks with this custom item in the main hand.
     *
     * @param player the player who left-clicked
     * @param stack  the item stack that was left-clicked with
     * @return {@code true} if the involved event should be canceled, {@code false} otherwise
     */
    public abstract boolean onLMB(@NotNull Player player,
                                  @NotNull ItemStack stack);

    /**
     * Called when a player swaps to this custom item.
     *
     * @param player the player who swapped to this item
     * @param stack  the item stack that was swapped to
     * @return {@code true} if the involved event should be canceled, {@code false} otherwise
     */
    public abstract boolean onSwapTo(@NotNull Player player,
                                     @NotNull ItemStack stack);

    /**
     * Called when a player swaps away from this custom item.
     *
     * @param player the player who swapped away from this item
     * @param stack  the item stack that was swapped away
     * @return {@code true} if the involved event should be canceled, {@code false} otherwise
     */
    public abstract boolean onSwapAway(@NotNull Player player,
                                       @NotNull ItemStack stack);

    // -----< Utilities >-----

    /**
     * Builds the prototype {@link ItemStack} for this custom item.
     *
     * @return a fully configured prototype stack
     */
    private @NotNull ItemStack createPrototype() {
        final ItemStack stack = createBaseItemStack();
        applyCustomAttributes(stack);
        return stack;
    }

    /**
     * Creates a new {@link ItemStack} with all the base attributes already set.
     *
     * @return a new stack with all the base attributes applied
     */
    private @NotNull ItemStack createBaseItemStack() {
        final ItemStack itemStack = new ItemStack(material);
        final ItemMeta meta = itemStack.getItemMeta();

        meta.setCustomModelData(customModelData);
        meta.displayName(displayName);
        meta.lore(lore);
        meta.setMaxStackSize(maxStackSize);

        itemStack.setItemMeta(meta);
        return itemStack;
    }
}