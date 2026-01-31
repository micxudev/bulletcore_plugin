package org.dredd.bulletcore.models.weapons.damage;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents different hit regions on a {@link LivingEntity}.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum DamagePoint {

    /**
     * Upper portion of the body (e.g., head/face).
     */
    HEAD {
        @Override
        public double getDamage(@NotNull WeaponDamage damage) {return damage.head();}

        @Override
        public @Nullable ItemStack getArmor(@NotNull PlayerInventory inv) {return inv.getHelmet();}

        @Override
        public void setArmor(@NotNull PlayerInventory inv, @Nullable ItemStack stack) {inv.setHelmet(stack);}
    },

    /**
     * Torso area.
     */
    BODY {
        @Override
        public double getDamage(@NotNull WeaponDamage damage) {return damage.body();}

        @Override
        public @Nullable ItemStack getArmor(@NotNull PlayerInventory inv) {return inv.getChestplate();}

        @Override
        public void setArmor(@NotNull PlayerInventory inv, @Nullable ItemStack stack) {inv.setChestplate(stack);}
    },

    /**
     * Upper legs or thighs.
     */
    LEGS {
        @Override
        public double getDamage(@NotNull WeaponDamage damage) {return damage.legs();}

        @Override
        public @Nullable ItemStack getArmor(@NotNull PlayerInventory inv) {return inv.getLeggings();}

        @Override
        public void setArmor(@NotNull PlayerInventory inv, @Nullable ItemStack stack) {inv.setLeggings(stack);}
    },

    /**
     * Lower legs or feet.
     */
    FEET {
        @Override
        public double getDamage(@NotNull WeaponDamage damage) {return damage.feet();}

        @Override
        public @Nullable ItemStack getArmor(@NotNull PlayerInventory inv) {return inv.getBoots();}

        @Override
        public void setArmor(@NotNull PlayerInventory inv, @Nullable ItemStack stack) {inv.setBoots(stack);}
    };

    /**
     * Retrieves the damage value corresponding to the current damage point from the given weapon damage.
     *
     * @param damage the {@link WeaponDamage} instance containing damage values for each damage point
     * @return the damage value applicable to the current damage point
     */
    public abstract double getDamage(@NotNull WeaponDamage damage);

    /**
     * Retrieves the armor piece corresponding to the current damage point from the given player inventory.
     *
     * @param inventory the player's inventory from which the armor piece will be retrieved
     * @return the item stack representing the armor piece for this damage point, or {@code null}
     * if no such armor piece is present
     */
    public abstract @Nullable ItemStack getArmor(@NotNull PlayerInventory inventory);

    /**
     * Sets the armor piece for the current damage point in the given player inventory.
     *
     * @param inventory the player's inventory for which the armor piece will be set
     * @param stack     the item stack representing the armor piece to set, or {@code null}
     *                  if armor piece should be removed
     */
    public abstract void setArmor(@NotNull PlayerInventory inventory, @Nullable ItemStack stack);
}