package org.dredd.bulletcore.armorstand_features.features;

import org.bukkit.Material;
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

    // ===== Defaults =====
    /**
     * Features are enabled by default
     */
    protected static final boolean DEFAULT_ENABLED = true;

    /**
     * Default model data (0 = vanilla material).
     */
    protected static final int DEFAULT_MODEL_DATA = 0;

    // ===== Instance =====
    /**
     * Whether this feature is enabled
     */
    public final boolean enabled;

    /**
     * The visual item displayed on the armor stand.
     */
    public final ItemStack item;

    /**
     * Creates a new armor stand feature.
     *
     * @param enabled   whether the feature is active
     * @param material  the base material for the item
     * @param modelData custom model data (0 = vanilla)
     */
    protected ArmorStandFeature(boolean enabled, @NotNull Material material, int modelData) {
        this.enabled = enabled;
        this.item = buildItem(material, modelData);
    }

    /**
     * Builds the item used for display, applying custom model data if present.
     */
    private static @NotNull ItemStack buildItem(@NotNull Material material, int modelData) {
        return modelData == DEFAULT_MODEL_DATA
            ? new ItemStack(material)
            : ServerUtils.createCustomModelItem(material, modelData);
    }
}