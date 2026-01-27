package org.dredd.bulletcore.models.weapons.shooting;

import java.util.UUID;

import org.dredd.bulletcore.models.weapons.Weapon;

public record CurrentHit(
    UUID damager,
    UUID victim,
    Weapon weapon
) {}