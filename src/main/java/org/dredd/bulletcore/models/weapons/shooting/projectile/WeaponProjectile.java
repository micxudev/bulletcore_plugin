package org.dredd.bulletcore.models.weapons.shooting.projectile;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.models.weapons.damage.HitHandler;
import org.jetbrains.annotations.NotNull;

public class WeaponProjectile extends AProjectile {

    // ----------< Instance >----------

    // -----< Attributes >-----

    private final @NotNull Weapon weapon;
    private final @NotNull Player shooter;
    private final @NotNull ProjectileSettings settings;
    private final @NotNull ProjectileRayTracer rayTrace;


    // -----< Construction >-----

    public WeaponProjectile(@NotNull Weapon weapon,
                            @NotNull Player shooter,
                            @NotNull Location location,
                            @NotNull Vector motion) {
        super(location, motion);

        this.weapon = weapon;
        this.shooter = shooter;
        this.settings = weapon.projectileSettings;
        this.rayTrace = new ProjectileRayTracer(weapon, shooter, getWorld());
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

    @Override
    public double getMaxDistance() {
        final double max = settings.maxDistance;
        return (max == NOT_USED) ? super.getMaxDistance() : max;
    }


    // -----< Behavior >-----

    @Override
    public boolean handleCollisions(@NotNull Location currentLocation,
                                    @NotNull Vector direction,
                                    double moveDistance) {
        // Check if there is a collision that the bullet will not survive
        final RayTraceResult result = rayTrace.cast(currentLocation, direction, moveDistance);

        // TODO: apply trail particle, add into config to enable/disable?
        //weapon.trailParticle.spawn(currentLocation, direction, moveDistance, weapon, getWorld());

        // No such hit, keep the projectile alive
        if (result == null) return false;

        // Is such Hit (either entity or block that STOPPED THE BULLET)
        HitHandler.handleHit(shooter, weapon, result, getWorld());

        return true;
    }
}