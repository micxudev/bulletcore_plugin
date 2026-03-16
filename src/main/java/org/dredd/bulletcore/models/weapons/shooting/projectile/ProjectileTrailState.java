package org.dredd.bulletcore.models.weapons.shooting.projectile;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.config.particles.ConfiguredParticle;
import org.dredd.bulletcore.config.particles.ParticleManager;
import org.dredd.bulletcore.models.weapons.BulletTrailParticle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Maintains state for spawning particle trails behind a moving projectile.
 * <p>
 * This class ensures particles are spawned at constant intervals ({@link #step}) along the
 * projectile's path regardless of how far the projectile moves each tick.
 * <p>
 * Because projectiles may travel varying distances per tick, particles are not
 * simply spawned every tick. Instead, the class tracks the remaining distance
 * until the next particle spawn position and carries that state between updates.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class ProjectileTrailState {

    // -----< Attributes >-----

    /**
     * Particle configuration used when spawning the trail.
     */
    private final ConfiguredParticle particle;

    /**
     * Distance between consecutive particles along the projectile path.
     * <p>
     * Measured in blocks.
     */
    private final double step;

    /**
     * Remaining distance until the next particle spawn location.
     * <p>
     * This value is preserved between updates so that particles remain evenly
     * spaced even when projectile movement varies between ticks.
     */
    private double distanceToNextParticle;

    /**
     * Whether particle spawning should be skipped entirely.
     * <p>
     * This is enabled when {@link #step} is tiny to avoid excessive
     * particle spawning.
     */
    private final boolean doNotSpawn;

    // -----< Construction >-----

    /**
     * Creates a new projectile trail state using the specified configuration.
     *
     * @param trailParticle particle trail configuration defining spacing,
     *                      offset, and particle type
     */
    public ProjectileTrailState(@NotNull BulletTrailParticle trailParticle) {
        this.particle = trailParticle.particle;
        this.step = trailParticle.step;
        this.distanceToNextParticle = trailParticle.offset;
        this.doNotSpawn = step < 0.01D;
    }

    // -----< Public API >-----

    /**
     * Spawns trail particles along the projectile's movement path.
     * <p>
     * Particles are spawned at fixed spatial intervals rather than once per tick.<br>
     * This ensures visually consistent trail spacing even when the projectile travels
     * large or tiny distances in a single update.
     * <p>
     * If the projectile collided with a block or entity during this movement,
     * particle spawning will stop at the collision point.
     *
     * @param currentLocation current projectile location
     * @param direction       projectile movement direction (not required to be normalized)
     * @param result          collision result if the projectile hit something this tick,
     *                        or {@code null} if no collision occurred
     * @param moveDistance    maximum distance the projectile attempted to move this tick
     */
    public void spawn(@NotNull Location currentLocation,
                      @NotNull Vector direction,
                      @Nullable RayTraceResult result,
                      double moveDistance) {
        if (doNotSpawn) return;

        // normalize direction
        double dirX = direction.getX();
        double dirY = direction.getY();
        double dirZ = direction.getZ();

        final double lenSq = dirX * dirX + dirY * dirY + dirZ * dirZ;
        if (lenSq <= 1.0E-6) return;

        final double invLen = 1.0D / Math.sqrt(lenSq);
        dirX *= invLen;
        dirY *= invLen;
        dirZ *= invLen;


        // calculate travel distance
        double travelDistance = moveDistance;
        if (result != null) {
            final Vector hit = result.getHitPosition();
            final double dx = hit.getX() - currentLocation.getX();
            final double dy = hit.getY() - currentLocation.getY();
            final double dz = hit.getZ() - currentLocation.getZ();
            travelDistance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
        if (travelDistance <= 0.0D) return;


        // spawn particles
        final World world = currentLocation.getWorld();
        final ConfiguredParticle particle = this.particle;
        final double step = this.step;

        double remainingDistance = travelDistance;
        double distanceToNextParticle = this.distanceToNextParticle;

        double x = currentLocation.getX();
        double y = currentLocation.getY();
        double z = currentLocation.getZ();

        while (remainingDistance >= distanceToNextParticle) {
            x += dirX * distanceToNextParticle;
            y += dirY * distanceToNextParticle;
            z += dirZ * distanceToNextParticle;

            ParticleManager.spawnParticle(world, particle, x, y, z);

            remainingDistance -= distanceToNextParticle;
            distanceToNextParticle = step;
        }

        this.distanceToNextParticle = distanceToNextParticle - remainingDistance;
    }
}