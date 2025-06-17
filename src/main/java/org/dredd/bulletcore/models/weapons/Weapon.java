package org.dredd.bulletcore.models.weapons;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.dredd.bulletcore.models.CustomBase;
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

    @Override
    public boolean onRMB(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Right-click with Weapon");
        return false;
    }

    @Override
    public boolean onLMB(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Left-click with Weapon");
        return false;
    }
}