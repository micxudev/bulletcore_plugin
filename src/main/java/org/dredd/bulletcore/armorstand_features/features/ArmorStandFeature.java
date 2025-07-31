package org.dredd.bulletcore.armorstand_features.features;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.dredd.bulletcore.utils.ServerUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for all visual features rendered using invisible armor stands
 * <p>
 * Subclasses can implement specific behavior (e.g., positioning, rotation, removal timing).
 *
 * @author dredd
 * @since 1.0.0
 */
public abstract class ArmorStandFeature {

    /**
     * Default value: feature is enabled
     */
    protected static final boolean DEF_ENABLED = true;

    /**
     * Default value: no custom model data (uses base Minecraft material)
     */
    protected static final int DEF_MODEL_DATA = 0;

    /**
     * Whether the feature is enabled
     */
    public final boolean enabled;

    /**
     * The item used as a visual for this feature (with or without custom model data)
     */
    public final ItemStack item;

    /**
     * Constructs a new armor stand feature with the given params.
     *
     * @param enabled   whether the feature is active
     * @param material  the material to use for the visual item
     * @param modelData custom model data (0 = use vanilla material)
     */
    protected ArmorStandFeature(boolean enabled, @NotNull Material material, int modelData) {
        this.enabled = enabled;
        this.item = createItem(material, modelData);
    }

    /**
     * Creates the visual item with optional custom model data.
     *
     * @param material  the base material
     * @param modelData custom model data, or 0 for vanilla item
     * @return the item stack to be used on the armor stand
     */
    private ItemStack createItem(@NotNull Material material, int modelData) {
        return modelData == 0 ? new ItemStack(material) : ServerUtils.createCustomModelItem(material, modelData);
    }
}