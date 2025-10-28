package org.dredd.bulletcore.models.weapons.damage;

import org.bukkit.entity.LivingEntity;

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
    HEAD,

    /**
     * Torso area.
     */
    BODY,

    /**
     * Upper legs or thighs.
     */
    LEGS,

    /**
     * Lower legs or feet.
     */
    FEET
}