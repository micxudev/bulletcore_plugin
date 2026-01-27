package org.dredd.bulletcore.models.armor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.dredd.bulletcore.models.weapons.damage.DamagePoint;

/**
 * Represents a hit on an armor piece.
 *
 * @param initialArmor The armor before the hit damage was applied
 * @param armorDamage  The damage that should be dealt to the armor piece
 * @param damagePoint  The body part where the damage occurred
 * @param victim       The victim of the hit
 */
public record ArmorHit(
    Armor initialArmor,
    double armorDamage,
    DamagePoint damagePoint,
    Player victim
) implements Runnable {

    @Override
    public void run() {
        final PlayerInventory inv = victim.getInventory();

        final ItemStack stack = switch (damagePoint) {
            case HEAD -> inv.getHelmet();
            case BODY -> inv.getChestplate();
            case LEGS -> inv.getLeggings();
            case FEET -> inv.getBoots();
        };

        // make sure the armor stack didn't change in the meantime
        if (!initialArmor.isThisArmor(stack)) return;

        final double currentDurability = initialArmor.getDurability(stack);
        final double newDurability = currentDurability - armorDamage;

        if (newDurability > 0)
            initialArmor.setDurability(stack, newDurability);
        else
            switch (damagePoint) {
                case HEAD -> inv.setHelmet(null);
                case BODY -> inv.setChestplate(null);
                case LEGS -> inv.setLeggings(null);
                case FEET -> inv.setBoots(null);
            }
    }
}