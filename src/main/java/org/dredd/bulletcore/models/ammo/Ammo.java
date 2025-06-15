package org.dredd.bulletcore.models.ammo;

import org.bukkit.inventory.ItemStack;
import org.dredd.bulletcore.models.CustomBase;
import org.jetbrains.annotations.NotNull;

/**
 * Represents ammo items.
 *
 * @author dredd
 * @since 1.0.0
 */
public class Ammo extends CustomBase {

    public Ammo(BaseAttributes attrs) {
        super(attrs);
    }

    @Override
    public @NotNull ItemStack createItemStack() {
        // Add only ammo-specific attributes
        return createBaseItemStack();
    }
}