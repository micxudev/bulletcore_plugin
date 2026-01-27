package org.dredd.bulletcore.models;

import org.bukkit.configuration.file.YamlConfiguration;
import org.dredd.bulletcore.custom_item_manager.exceptions.ItemLoadException;
import org.jetbrains.annotations.NotNull;

/**
 * Functional interface for constructing {@link CustomBase} items from a {@link YamlConfiguration}.
 */
@FunctionalInterface
public interface CustomItemLoader {

    /**
     * Loads an item from the given configuration.
     *
     * @param config the YAML configuration
     * @return the loaded item
     * @throws ItemLoadException if the config is invalid or incomplete
     */
    @NotNull CustomBase load(@NotNull YamlConfiguration config) throws ItemLoadException;
}