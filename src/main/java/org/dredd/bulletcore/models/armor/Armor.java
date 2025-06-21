package org.dredd.bulletcore.models.armor;

import org.bukkit.entity.Player;
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

    @Override
    public boolean onRMB(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Right-click with Armor");
        return false;
    }

    @Override
    public boolean onLMB(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Left-click with Armor");
        return false;
    }

    @Override
    public boolean onSwapTo(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Swapped to Armor");
        return false;
    }

    @Override
    public boolean onSwapAway(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Swapped away from Armor");
        return false;
    }
}