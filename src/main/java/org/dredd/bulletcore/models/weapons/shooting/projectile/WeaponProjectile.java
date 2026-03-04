package org.dredd.bulletcore.models.weapons.shooting.projectile;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.models.weapons.shooting.ShootingHandler;
import org.jetbrains.annotations.NotNull;

public class WeaponProjectile extends AProjectile {

    // ----------< Instance >----------

    // -----< Attributes >-----

    private final @NotNull Weapon weapon;
    private final @NotNull ProjectileSettings settings;
    private final @NotNull ProjectileRayTracer rayTrace;


    // -----< Construction >-----

    public WeaponProjectile(@NotNull Weapon weapon,
                            @NotNull Player shooter,
                            @NotNull Location location,
                            @NotNull Vector motion) {
        super(shooter, location, motion);

        this.weapon = weapon;
        this.settings = weapon.projectileSettings;
        this.rayTrace = new ProjectileRayTracer(weapon, shooter); // TODO: setup here for access to instance methods (e.g. getAliveTicks())
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

        // Check if there is a collision
        // that the bullet will not survive
        final RayTraceResult result = rayTrace.cast(
            getWorld(),
            getLocation(),
            newLocation,
            getNormalizedMotion()
        );

        // TODO: apply trail particle, add into config to enable/disable?
        //weapon.trailParticle.spawn(eyeLocation, direction, result, weapon, getWorld());

        // No hit
        if (result == null) {
            setRawLocation(newLocation);
            addDistanceTravelled(motionLength);
            return hasTraveledMaxDistance();
        }

        // Hit (either entity or block that STOPPED THE BULLET)
        ShootingHandler.handleHit(getShooter(), weapon, result, getWorld());

        return true;
    }
}