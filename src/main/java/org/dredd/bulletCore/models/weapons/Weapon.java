package org.dredd.bulletCore.models.weapons;

import org.bukkit.inventory.ItemStack;
import org.dredd.bulletCore.models.CustomBase;
import org.jetbrains.annotations.NotNull;

/**
 * Represents weapon items.
 *
 * @author dredd
 * @since 1.0.0
 */
public class Weapon extends CustomBase {

    public Weapon(BaseAttributes attrs) {
        super(attrs);
    }

    @Override
    public @NotNull ItemStack createItemStack() {
        // Add only weapon-specific attributes
        return createBaseItemStack();
    }
}