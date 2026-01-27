package org.dredd.bulletcore.models.weapons.shooting;

import java.util.UUID;

import org.dredd.bulletcore.models.weapons.Weapon;

/**
 * Data container representing a hit that is currently being processed.
 *
 * @param damager the UUID of the player damaging the victim
 * @param victim  the UUID of the entity being damaged
 * @param weapon  the weapon used to damage the victim
 * @author dredd
 * @since 1.0.0
 */
public record CurrentHit(
    UUID damager,
    UUID victim,
    Weapon weapon
) {}