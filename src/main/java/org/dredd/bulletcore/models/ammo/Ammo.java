package org.dredd.bulletcore.models.ammo;

import org.bukkit.entity.Player;
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

    @Override
    public boolean onRMB(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Right-click with Ammo");
        return false;
    }

    @Override
    public boolean onLMB(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Left-click with Ammo");
        return false;
    }

    @Override
    public boolean onSwapTo(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Swapped to Ammo");
        return false;
    }

    @Override
    public boolean onSwapAway(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Swapped away from Ammo");
        return false;
    }
}