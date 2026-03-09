package org.dredd.bulletcore.models.weapons.shooting.projectile;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.compatibility.entity.FakeEntity;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.models.weapons.damage.HitHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WeaponProjectile extends AProjectile {

    // ----------< Instance >----------

    // -----< Attributes >-----

    private final Weapon weapon;
    private final Player shooter;
    private final ProjectileSettings settings;
    private final ProjectileRayTracer rayTrace;
    private final ProjectileTrailState trailState;


    // -----< Construction >-----

    public WeaponProjectile(@NotNull Weapon weapon,
                            @NotNull Player shooter,
                            @NotNull Location location,
                            @NotNull Vector motion,
                            @Nullable FakeEntity disguise) {
        super(location, motion, disguise);

        this.weapon = weapon;
        this.shooter = shooter;
        this.settings = weapon.projectileSettings;
        this.rayTrace = new ProjectileRayTracer(weapon, shooter, getWorld());
        this.trailState = new ProjectileTrailState(weapon.trailParticle);
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

        trailState.spawn(currentLocation, direction, result, moveDistance);

        // No such hit, keep the projectile alive
        if (result == null) return false;

        // Is such Hit (either entity or block that STOPPED THE BULLET)
        HitHandler.handleHit(shooter, weapon, result, getWorld());

        return true;
    }
}