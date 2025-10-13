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

import java.util.List;
import java.util.stream.Collectors;

/**
 * The base class for all custom models.
 *
 * @author dredd
 * @since 1.0.0
 */
public abstract class CustomBase {

    /**
     * The internal, unique identifier for the item used in commands (e.g., `give {@code ak47}`).
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
     * A string representation of the {@link #displayName} field with all styles being removed.
     */
    public final String displayNameString;

    /**
     * A list of {@link Component} that will appear as the item's lore (description below the name).
     */
    public final List<Component> lore;

    /**
     * This is the maximum amount that an item will stack,
     * must be between {@code 1} and {@code 99} (inclusive).
     */
    public final int maxStackSize;

    protected CustomBase(@NotNull YamlConfiguration config) throws ItemLoadException {
        this.name = config.getString("name");
        if (!CustomItemsRegistry.canNameBeUsed(name))
            throw new ItemLoadException(
                "Name: '" + name + "' does not match " + CustomItemsRegistry.VALID_NAME.pattern() + " or is already in use"
            );

        this.customModelData = config.getInt("customModelData");
        if (!CustomItemsRegistry.canModelDataBeUsed(customModelData))
            throw new ItemLoadException(
                "CustomModelData: " + customModelData + " is < 0 or does not end with 2 zeroes or is already in use"
            );

        this.material = ServerUtils.getMetaCapableMaterial(config.getString("material"));

        this.displayName = config.getRichMessage("displayName", ComponentUtils.plainWhite(name));

        this.displayNameString = PlainTextComponentSerializer.plainText().serialize(displayName);

        this.lore = config.getStringList("lore").stream()
            .map(ComponentUtils::deserialize)
            .collect(Collectors.toList());

        this.maxStackSize = Math.clamp(config.getInt("maxStackSize", material.getMaxStackSize()), 1, 99);
    }

    /**
     * Creates a new {@link ItemStack} with all the base fields already set.
     *
     * @return A new {@link ItemStack} with the base fields applied
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

    /**
     * Called when the player swaps to the custom item.
     *
     * @param player   The player who swapped to the item
     * @param usedItem The item that was swapped to
     * @return {@code true} if the involved event should be canceled, {@code false} otherwise
     */
    public abstract boolean onSwapTo(@NotNull Player player, @NotNull ItemStack usedItem);

    /**
     * Called when the player swaps away from the custom item.
     *
     * @param player   The player who swapped away from the item
     * @param usedItem The item that was swapped away
     * @return {@code true} if the involved event should be canceled, {@code false} otherwise
     */
    public abstract boolean onSwapAway(@NotNull Player player, @NotNull ItemStack usedItem);
}