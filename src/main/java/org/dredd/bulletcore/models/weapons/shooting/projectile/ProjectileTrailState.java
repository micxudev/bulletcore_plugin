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

public final class ProjectileTrailState {

    private final double step;

    private final ConfiguredParticle particle;

    private double distanceToNextParticle;

    private final boolean doNotSpawn;

    public ProjectileTrailState(@NotNull BulletTrailParticle trailParticle) {
        this.step = trailParticle.step;
        this.particle = trailParticle.particle;
        this.distanceToNextParticle = trailParticle.offset;
        this.doNotSpawn = step < 0.01D;
    }

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