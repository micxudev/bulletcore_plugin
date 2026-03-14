package org.dredd.bulletcore.models.weapons.shooting.projectile;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.compatibility.entity.FakeEntity;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.models.weapons.damage.HitHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WeaponProjectile extends AProjectile {

    // -----< Attributes >-----

    private final Weapon weapon;
    private final Player shooter;
    private final ProjectileSettings settings;
    private final ProjectileRayTracer rayTracer;
    private final ProjectileTrailState trailState;


    // -----< Construction >-----

    public WeaponProjectile(@NotNull Weapon weapon,
                            @NotNull Player shooter,
                            @NotNull Location location,
                            @NotNull Vector velocity,
                            @Nullable FakeEntity disguise) {
        super(location, velocity, disguise);

        this.weapon = weapon;
        this.shooter = shooter;
        this.settings = weapon.projectileSettings;
        this.rayTracer = new ProjectileRayTracer(weapon, shooter, getWorld());
        this.trailState = new ProjectileTrailState(weapon.trailParticle);
    }


    // -----< Settings Override >-----

    @Override
    protected double getGravity() {return settings.gravity;}

    @Override
    protected double getMinSpeed() {return settings.minSpeed;}

    @Override
    protected boolean doRemoveAtMinSpeed() {return settings.removeAtMinSpeed;}

    @Override
    protected double getMaxSpeed() {return settings.maxSpeed;}

    @Override
    protected boolean doRemoveAtMaxSpeed() {return settings.removeAtMaxSpeed;}

    @Override
    protected double getDrag() {
        if (getCurrentBlock().isLiquid())
            return settings.dragInWater;

        final World world = getWorld();
        if (world.isThundering() || world.hasStorm())
            return settings.dragWhenRainingOrSnowing;

        return settings.drag;
    }

    @Override
    protected int getMaximumAliveTicks() {return settings.maxAliveTicks;}

    @Override
    protected double getMaxDistance() {
        final double max = settings.maxDistance;
        return (max == NOT_USED) ? super.getMaxDistance() : max;
    }


    // -----< Behavior >-----

    @Override
    protected boolean handleCollisions(@NotNull Location location,
                                       @NotNull Vector velocity,
                                       double moveDistance) {
        final RayTraceResult result = rayTracer.cast(location, velocity, moveDistance);

        trailState.spawn(location, velocity, result, moveDistance);

        if (result == null) return false;

        HitHandler.handleHit(shooter, weapon, result, getWorld());

        return true;
    }
}