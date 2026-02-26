package org.dredd.bulletcore.utils.ray;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

// TODO: replace by org.bukkit.util.RayTraceResult

/**
 * Represents the result of a ray tracing operation against a {@link HitBox}.
 * <p>
 * Important:
 * The provided direction vector MUST be normalized.<br>
 * Otherwise, hitMin and hitMax represent parametric values and not true world distances.
 */
public abstract sealed class RayTraceResult
    permits BlockTraceResult, EntityTraceResult {

    // -----< Attributes >-----

    private final @NotNull HitBox hitBox;
    private final @NotNull BlockFace hitFace;
    private final double hitMin;
    private final double throughDistance;
    private final @NotNull Vector hitLocation;


    // -----< Construction >-----

    RayTraceResult(@NotNull Vector origin,
                   @NotNull Vector direction,
                   @NotNull HitBox hitBox,
                   @NotNull BlockFace hitFace,
                   double hitMin,
                   double hitMax) {
        this.hitBox = hitBox;
        this.hitFace = hitFace;
        this.hitMin = hitMin;
        this.throughDistance = Math.max(hitMax - hitMin, 0.0D);
        this.hitLocation = direction.clone().multiply(hitMin).add(origin);
    }


    // -----< Getters >-----

    public final @NotNull HitBox getHitBox() {return hitBox;}

    public final @NotNull BlockFace getHitFace() {return hitFace;}

    /**
     * Distance between the origin of the ray and the hit location.<br>
     * May be negative if the ray started inside the hitbox.<br>
     * So called the <b>entry wound</b> of the ray.
     */
    public final double getHitMin() {return hitMin;}

    /**
     * Entry distance clamped to zero.
     */
    public final double getHitMinClamped() {return Math.max(hitMin, 0.0D);}

    /**
     * Distance traveled through the hitbox. Always non-negative.
     */
    public final double getThroughDistance() {return throughDistance;}

    /**
     * Returns the exact world location where the ray entered the hitbox.
     */
    public final @NotNull Vector getHitLocation() {return hitLocation.clone();}
}