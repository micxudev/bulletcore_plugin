package org.dredd.bulletCore.models.grenades;

import org.bukkit.inventory.ItemStack;
import org.dredd.bulletCore.models.CustomBase;
import org.jetbrains.annotations.NotNull;

/**
 * Represents grenade items.
 *
 * @author dredd
 * @since 1.0.0
 */
public class Grenade extends CustomBase {

    public Grenade(BaseAttributes attrs) {
        super(attrs);
    }

    @Override
    public @NotNull ItemStack createItemStack() {
        // Add only grenade-specific attributes
        return createBaseItemStack();
    }
}