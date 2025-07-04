package org.dredd.bulletcore.models.weapons.damage;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

/**
 * Represents specific hit regions on a player's body for damage calculation.
 * This enum is used to determine where a hit landed on a player model,
 * and can be used to apply different damage values or effects based on hit location.
 *
 * <p>Damage regions include:</p>
 * <ul>
 *     <li>{@link #HEAD} - Hits to the upper portion of the body (e.g., head/face)</li>
 *     <li>{@link #BODY} - Hits to the torso area</li>
 *     <li>{@link #LEGS} - Hits to the upper legs or thighs</li>
 *     <li>{@link #FEET} - Hits to the lower legs or feet</li>
 * </ul>
 */
public enum DamagePoint {
    /**
     * If the damage is hitting the head
     */
    HEAD,

    /**
     * If the damage is hitting the body
     */
    BODY,

    /**
     * If the damage is hitting legs
     */
    LEGS,

    /**
     * If the damage is hitting feet
     */
    FEET;


    /**
     * Determines the {@link DamagePoint} corresponding to the vertical hit position on the victim.
     * <p>
     * This method uses the Y-coordinate of the hit relative to the victim's bounding box
     * to categorize the hit location. If the player is sleeping, all hits are treated as {@link #HEAD},
     * as the hitbox is tiny and localized to the head region.
     *
     * @param victim   the player who was hit
     * @param hitPoint the location of the hit (typically from ray tracing)
     * @return the body part that was hit, as a {@link DamagePoint}
     */
    public static DamagePoint getDamagePoint(@NotNull Player victim, @NotNull Location hitPoint) {
        // Player hitbox size (height, width):
        // sleeping: h=0.2, w=0.2
        // standing: h=1.8, w=0.6
        // sneaking: h=1.5, w=0.6
        // lying:    h=0.6, w=0.6

        if (victim.isSleeping()) return HEAD; // while sleeping hitbox is only in the head

        BoundingBox bb = victim.getBoundingBox();
        double normalizedY = (hitPoint.getY() - bb.getMinY()) / bb.getHeight();

        if (normalizedY > 0.78) return HEAD;
        if (normalizedY > 0.4) return BODY;
        if (normalizedY > 0.08) return LEGS;
        return FEET;
    }
}