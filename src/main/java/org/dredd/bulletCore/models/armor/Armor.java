package org.dredd.bulletCore.models.armor;

import org.bukkit.inventory.ItemStack;
import org.dredd.bulletCore.models.CustomBase;
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