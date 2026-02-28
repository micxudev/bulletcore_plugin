package org.dredd.bulletcore.models.weapons.shooting.projectile;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.utils.ray.BlockTraceResult;
import org.dredd.bulletcore.utils.ray.RayTrace;
import org.dredd.bulletcore.utils.ray.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WeaponProjectile extends AProjectile {

    // ----------< Instance >----------

    // -----< Attributes >-----

    private final @NotNull ProjectileSettings settings;
    private final @NotNull RayTrace rayTrace;


    // -----< Construction >-----

    public WeaponProjectile(@NotNull ProjectileSettings settings,
                            @Nullable Player shooter,
                            @NotNull Location location,
                            @NotNull Vector motion) {
        super(shooter, location, motion);

        this.settings = settings;

        if (settings.disableEntityCollisions) {
            this.rayTrace = new RayTrace()
                .disableEntityChecks()
                .enableLiquidChecks()
                .withRaySize(settings.raySize);
        } else {
            this.rayTrace = new RayTrace()
                .withEntityFilter(entity -> {
                    // No shooter == allow hit
                    if (shooter == null) return false;

                    // Temp self-collision immunity
                    if (getAliveTicks() < 10 && entity.getEntityId() == shooter.getEntityId())
                        return true;

                    // Don't hit shooter's transport.
                    // If the shooter is riding
                    // (e.g., horse and this horse has a passenger who is a shooter -> return true == prevent hit)
                    return entity.getPassengers().contains(shooter);
                })
                .enableLiquidChecks()
                .withRaySize(settings.raySize);
        }
    }


    // -----< Settings Override >-----

    @Override
    public double getGravity() {return settings.gravity;}

    @Override
    public double getMinSpeed() {return settings.minSpeed;}

    @Override
    public boolean doRemoveAtMinSpeed() {return settings.removeAtMinSpeed;}

    @Override
    public double getMaxSpeed() {return settings.maxSpeed;}

    @Override
    public boolean doRemoveAtMaxSpeed() {return settings.removeAtMaxSpeed;}

    @Override
    public double getDrag() {
        if (getCurrentBlock().isLiquid())
            return settings.decreaseInWater;

        if (getWorld().isThundering() || getWorld().hasStorm())
            return settings.decreaseWhenRainingOrSnowing;

        return settings.decrease;
    }

    @Override
    public int getMaximumAliveTicks() {return settings.maxAliveTicks;}


    // -----< Behavior >-----

    public boolean hasTraveledMaxDistance() {
        final double max = settings.maxDistance;
        return max != NOT_USED && getDistanceTravelled() >= max;
    }

    @Override
    public boolean updatePosition() {
        final Vector newLocation = getLocation().add(getMotion());

        if (!getWorld().isChunkLoaded(newLocation.getBlockX() >> 4, newLocation.getBlockZ() >> 4)) {
            // Remove projectile if new location would be in an unloaded chunk
            return true;
        }

        // Do not check for new collisions if there is no motion
        final double motionLength = getMotionLength();
        if (motionLength < Vector.getEpsilon()) return false;

        // Hits sorted by distance (closer first)
        final List<RayTraceResult> hits = rayTrace.cast(
            getWorld(),
            getLocation(),
            newLocation,
            getNormalizedMotion(),
            0
        );

        if (hits.isEmpty()) {
            // No hits, simply update location and distance traveled
            setRawLocation(newLocation);
            addDistanceTravelled(motionLength);
            return hasTraveledMaxDistance();
        }

        double distanceAlreadyAdded = 0;
        for (RayTraceResult hit : hits) {
            // Stay on track of current location and distance traveled on each iteration
            setRawLocation(hit.getHitLocation());
            double add = hit.getHitMinClamped() - distanceAlreadyAdded;
            addDistanceTravelled(distanceAlreadyAdded += add);

            // Remove projectile since it cannot go this far
            if (hasTraveledMaxDistance()) return true;

            // Skip liquid hit; TODO: it should depend on block predicate
            if (hit instanceof BlockTraceResult blockHit && blockHit.getBlock().isLiquid()) continue;

            // TODO: HANDLE HIT HERE!!!
            //if (WeaponMechanics.getInstance().getWeaponHandler().getHitHandler().handleHit(hit, this))
            //    continue;


            // Projectile should die if code reaches this point
            // since the handled hit above returned false
            // meaning that it consumed the projectile and its damage
            return true;
        }

        // Here we know that projectile didn't die on any collision.
        // We still have to update the location to last possible location.
        setRawLocation(newLocation);
        addDistanceTravelled(motionLength - distanceAlreadyAdded);
        return hasTraveledMaxDistance();
    }
}