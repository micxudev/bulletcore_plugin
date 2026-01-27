package org.dredd.bulletcore.models.grenades;

import org.bukkit.configuration.file.YamlConfiguration;
import org.dredd.bulletcore.custom_item_manager.exceptions.ItemLoadException;
import org.dredd.bulletcore.models.CustomBase;
import org.jetbrains.annotations.NotNull;

/**
 * Represents grenade items.
 *
 * @author dredd
 * @since 1.0.0
 */
public class Grenade extends CustomBase {

    // -----< Construction >-----

    /**
     * Loads and validates a grenade item definition from the given config.
     *
     * @param config the YAML configuration source
     * @throws ItemLoadException if validation fails
     */
    public Grenade(@NotNull YamlConfiguration config) throws ItemLoadException {
        super(config);
    }

    // -----< Grenade Behavior >-----

    // Override only needed methods from CustomBase
}