package org.dredd.bulletcore.models.armor;

import org.bukkit.inventory.ItemStack;
import org.dredd.bulletcore.models.CustomBase;
import org.jetbrains.annotations.NotNull;

/**
 * Represents armor items.
 *
 * @author dredd
 * @since 1.0.0
 */
public class Armor extends CustomBase {

    public Armor(BaseAttributes attrs) {
        super(attrs);
    }

    @Override
    public @NotNull ItemStack createItemStack() {
        // Add only armor-specific attributes
        return createBaseItemStack();
    }
}