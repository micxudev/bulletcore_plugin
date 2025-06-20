package org.dredd.bulletcore.models.weapons;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dredd.bulletcore.models.CustomBase;
import org.jetbrains.annotations.NotNull;

import static org.bukkit.inventory.ItemFlag.HIDE_ADDITIONAL_TOOLTIP;
import static org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE;

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

    /**
     * Triggered when a player attempts to drop a weapon.
     * This method is invoked specifically when the drop action is initiated manually with the drop key (Q).
     *
     * @param player   the player who attempts to drop the weapon
     * @param usedItem the item being dropped, which is this weapon object
     */
    public void onDrop(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Weapon drop attempt on key (Q)");
    }

    @Override
    public @NotNull ItemStack createItemStack() {
        ItemStack stack = createBaseItemStack();
        ItemMeta meta = stack.getItemMeta();

        // Add only weapon-specific attributes
        meta.setUnbreakable(true);
        meta.addItemFlags(HIDE_UNBREAKABLE, HIDE_ADDITIONAL_TOOLTIP);

        stack.setItemMeta(meta);
        return stack;
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