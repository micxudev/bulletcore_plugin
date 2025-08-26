package org.dredd.bulletcore.models.armor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.models.weapons.damage.DamagePoint;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a hit on an armor piece.
 *
 * @param initialArmor The armor before the hit damage was applied
 * @param armorDamage  The damage that should be dealt to the armor piece
 * @param damagePoint  The body part where the damage occurred
 * @param victim       The victim of the hit
 */
public record ArmorHit(
    @NotNull Armor initialArmor,
    double armorDamage,
    @NotNull DamagePoint damagePoint,
    @NotNull Player victim
) implements Runnable {

    @Override
    public void run() {
        PlayerInventory inv = victim.getInventory();
        ItemStack armorStack = switch (damagePoint) {
            case HEAD -> inv.getHelmet();
            case BODY -> inv.getChestplate();
            case LEGS -> inv.getLeggings();
            case FEET -> inv.getBoots();
        };

        Armor currentArmor = CustomItemsRegistry.getArmorOrNull(armorStack);
        if (armorStack == null || currentArmor != initialArmor)
            return; // armor was changed in the meantime, cancel the armor damage

        double currentDurability = currentArmor.getDurability(armorStack);
        double newDurability = currentDurability - armorDamage;

        if (newDurability > 0)
            currentArmor.setDurability(armorStack, newDurability);
        else
            switch (damagePoint) {
                case HEAD -> inv.setHelmet(null);
                case BODY -> inv.setChestplate(null);
                case LEGS -> inv.setLeggings(null);
                case FEET -> inv.setBoots(null);
            }
    }
}