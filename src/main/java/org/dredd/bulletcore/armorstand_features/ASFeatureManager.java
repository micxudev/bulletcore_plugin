package org.dredd.bulletcore.armorstand_features;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.dredd.bulletcore.armorstand_features.features.BulletHoleFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Manages all armorstand-based visual features and loads them from config.
 * <p>
 * Missing or invalid sections fall back to defaults.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class ASFeatureManager {

    // ----------< Static >----------

    // -----< Loaders >-----

    /**
     * Loads all armorstand features from the given configuration.
     *
     * @param cfg the plugin config
     * @return a new {@link ASFeatureManager} instance
     */
    public static @NotNull ASFeatureManager load(@NotNull FileConfiguration cfg) {
        return new ASFeatureManager(cfg.getConfigurationSection("armor-stand-features"));
    }

    /**
     * Loads a feature from a named subsection using the provided loader.
     *
     * @param root   the parent config section; can be null
     * @param name   the subsection name to load
     * @param loader a function that converts a section (or null) into a feature
     * @param <T>    the feature type
     * @return the loaded feature
     */
    private static <T> @NotNull T loadFeature(@Nullable ConfigurationSection root,
                                              @NotNull String name,
                                              @NotNull Function<@Nullable ConfigurationSection, @NotNull T> loader) {
        final ConfigurationSection section = (root != null)
            ? root.getConfigurationSection(name)
            : null;
        return loader.apply(section);
    }


    // ----------< Instance >----------

    // -----< Features >-----

    /**
     * Bullet hole visual (spawned at block hit locations).
     */
    public final BulletHoleFeature bulletHole;

    // -----< Construction >-----

    /**
     * Private constructor. Use {@link #load(FileConfiguration)} instead.
     *
     * @param root the root config section for armor stand features; can be null
     */
    private ASFeatureManager(@Nullable ConfigurationSection root) {
        this.bulletHole = loadFeature(root, "bullet-hole", BulletHoleFeature::load);
    }
}