package org.dredd.bulletcore.armorstand_features.features;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.dredd.bulletcore.utils.ServerUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for visual features rendered with invisible armor stands.
 * <p>
 * Subclasses define specific visuals (e.g., position, rotation, lifespan).
 *
 * @author dredd
 * @since 1.0.0
 */
public abstract class ArmorStandFeature {

    // ----------< Static >----------

    // -----< Defaults >-----

    /**
     * Features are enabled by default
     */
    private static final boolean DEFAULT_ENABLED = true;

    /**
     * Default custom model data (0 = vanilla material).
     */
    private static final int DEFAULT_CUSTOM_MODEL_DATA = 0;


    // ----------< Instance >----------

    // -----< Attributes >-----

    /**
     * Whether this feature is enabled
     */
    final boolean enabled;

    /**
     * The visual item displayed on the armor stand.
     */
    final ItemStack item;

    // -----< Construction >-----

    ArmorStandFeature(@NotNull Material material) {
        this.enabled = DEFAULT_ENABLED;
        this.item = buildItem(material, DEFAULT_CUSTOM_MODEL_DATA);
    }

    ArmorStandFeature(@NotNull ConfigurationSection section,
                      @NotNull Material material) {
        this.enabled = section.getBoolean("enabled", DEFAULT_ENABLED);
        this.item = buildItem(material, section.getInt("customModelData", DEFAULT_CUSTOM_MODEL_DATA));
    }

    // -----< Utilities >-----

    /**
     * Builds the item used for display, applying custom model data if present.
     */
    private @NotNull ItemStack buildItem(@NotNull Material material,
                                         int modelData) {
        return modelData == DEFAULT_CUSTOM_MODEL_DATA
            ? new ItemStack(material)
            : ServerUtils.createCustomModelItem(material, modelData);
    }
}