package org.dredd.bulletcore.models.grenades;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

    @Override
    public @NotNull ItemStack createItemStack() {
        // Add only grenade-specific attributes
        return super.createBaseItemStack();
    }

    @Override
    public boolean onRMB(@NotNull Player player,
                         @NotNull ItemStack stack) {
        //System.out.println("Right-click with Grenade");
        return false;
    }

    @Override
    public boolean onLMB(@NotNull Player player,
                         @NotNull ItemStack stack) {
        //System.out.println("Left-click with Grenade");
        return false;
    }

    @Override
    public boolean onSwapTo(@NotNull Player player,
                            @NotNull ItemStack stack) {
        //System.out.println("Swapped to Grenade");
        return false;
    }

    @Override
    public boolean onSwapAway(@NotNull Player player,
                              @NotNull ItemStack stack) {
        //System.out.println("Swapped away from Grenade");
        return false;
    }
}