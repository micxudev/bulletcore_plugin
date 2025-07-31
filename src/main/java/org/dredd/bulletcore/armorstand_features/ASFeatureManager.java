package org.dredd.bulletcore.armorstand_features;

import org.bukkit.configuration.ConfigurationSection;
import org.dredd.bulletcore.armorstand_features.features.BulletHoleFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds all armor stand-based visual features and loads them from the config.
 * If a feature section is missing or invalid, a default instance is used.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class ASFeatureManager {

    /**
     * Bullet hole feature (shown at the block hit location)
     */
    public final BulletHoleFeature bulletHole;

    /**
     * Creates and loads all available armor stand features from the given config section.
     *
     * @param section the root config section for armor stand features
     */
    public ASFeatureManager(@Nullable ConfigurationSection section) {
        this.bulletHole = BulletHoleFeature.load(getFeatureSection(section, "bullet_hole"));
    }

    /**
     * Gets a section for a specific feature safely.
     *
     * @param parent the parent config section
     * @param name   the subsection name to fetch
     * @return the subsection if found, or null
     */
    private static @Nullable ConfigurationSection getFeatureSection(@Nullable ConfigurationSection parent, @NotNull String name) {
        return parent == null ? null : parent.getConfigurationSection(name);
    }
}