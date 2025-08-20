package org.dredd.bulletcore.models.weapons.damage;

import org.bukkit.inventory.ItemStack;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the result of a hit using {@link Weapon}.
 *
 * @param initialDamage The initial damage caused by the hit
 * @param armorStack    The armor {@link ItemStack} worn (if any) by the victim during the hit into {@link DamagePoint}
 */
public record HitResult(
    double initialDamage,
    @Nullable ItemStack armorStack
) {}