package org.dredd.bulletcore.models.armor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.dredd.bulletcore.models.weapons.damage.DamagePoint;

/**
 * Represents a hit on an armor piece.
 *
 * @param initialArmor  The armor before the hit damage was applied
 * @param initialDamage The initial damage that should be dealt to the armor piece
 * @param damagePoint   The body part where the damage occurred
 * @param victim        The victim of the hit
 */
public record ArmorHit(
    Armor initialArmor,
    double initialDamage,
    DamagePoint damagePoint,
    Player victim
) {

    /**
     * Applies the custom armor damage to the armor stack.
     *
     * @param blockedDamage the amount of damage blocked by other means (e.g., shield)
     */
    public void applyArmorDamage(double blockedDamage) {
        final double remainingArmorDamage = initialDamage - blockedDamage;
        if (remainingArmorDamage <= 0) return;

        final PlayerInventory inv = victim.getInventory();
        final ItemStack stack = damagePoint.getArmor(inv);

        // make sure the armor stack didn't change in the meantime
        if (!initialArmor.isThisArmor(stack)) return;

        final double currentDurability = initialArmor.getDurability(stack);
        final double newDurability = currentDurability - remainingArmorDamage;

        if (newDurability > 0)
            initialArmor.setDurability(stack, newDurability);
        else
            damagePoint.setArmor(inv, null);
    }
}