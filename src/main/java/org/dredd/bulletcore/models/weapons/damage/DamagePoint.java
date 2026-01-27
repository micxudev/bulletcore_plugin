package org.dredd.bulletcore.models.weapons.damage;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

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
        public double getDamage(@NonNull WeaponDamage damage) {return damage.head();}

        @Override
        public @Nullable ItemStack getArmor(@NonNull PlayerInventory inv) {return inv.getHelmet();}
    },

    /**
     * Torso area.
     */
    BODY {
        @Override
        public double getDamage(@NonNull WeaponDamage damage) {return damage.body();}

        @Override
        public @Nullable ItemStack getArmor(@NonNull PlayerInventory inv) {return inv.getChestplate();}
    },

    /**
     * Upper legs or thighs.
     */
    LEGS {
        @Override
        public double getDamage(@NonNull WeaponDamage damage) {return damage.legs();}

        @Override
        public @Nullable ItemStack getArmor(@NonNull PlayerInventory inv) {return inv.getLeggings();}
    },

    /**
     * Lower legs or feet.
     */
    FEET {
        @Override
        public double getDamage(@NonNull WeaponDamage damage) {return damage.feet();}

        @Override
        public @Nullable ItemStack getArmor(@NonNull PlayerInventory inv) {return inv.getBoots();}
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
}