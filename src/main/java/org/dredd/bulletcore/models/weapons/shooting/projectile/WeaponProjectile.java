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

/**
 * Projectile implementation used for {@link Weapon} fired projectiles.
 * <p>
 * This class connects the generic projectile simulation provided by
 * {@link AProjectile} with weapon-specific configuration and behavior.
 * <p>
 * The projectile physics settings are defined by
 * {@link ProjectileSettings} associated with the weapon.
 * <p>
 * Collision detection is delegated to {@link ProjectileRayTracer}, while
 * visual particle effects are handled by {@link ProjectileTrailState}.
 * <p>
 * When a collision is detected, the impact is processed by {@link HitHandler}.
 *
 * @author dredd
 * @since 1.0.0
 */
public class WeaponProjectile extends AProjectile {

    // -----< Attributes >-----

    /**
     * The weapon used to fire this projectile.
     */
    private final Weapon weapon;

    /**
     * The player who fired the projectile.
     */
    private final Player shooter;

    /**
     * Configuration defining the projectile's physical behavior.
     */
    private final ProjectileSettings settings;

    /**
     * Performs ray tracing for projectile collision detection.
     */
    private final ProjectileRayTracer rayTracer;

    /**
     * Maintains state required for spawning projectile trail particles.
     */
    private final ProjectileTrailState trailState;


    // -----< Construction >-----

    /**
     * Creates a new weapon projectile instance.
     *
     * @param weapon    the weapon used to fire the projectile
     * @param shooter   the player who fired the projectile
     * @param location  the initial projectile location
     * @param velocity  the initial projectile velocity (blocks per tick)
     * @param disguise  optional visual representation of the projectile
     */
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
    protected boolean removeWhenMinSpeedReached() {return settings.removeWhenMinSpeedReached;}

    @Override
    protected double getMaxSpeed() {return settings.maxSpeed;}

    @Override
    protected boolean removeWhenMaxSpeedReached() {return settings.removeWhenMaxSpeedReached;}

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

    /**
     * Performs and handles collision detection in the following way:
     * <ol>
     *     <li>Cast a ray along the projectile movement path to detect a hit.</li>
     *     <li>Spawn trail particles along the traveled path.</li>
     *     <li>If a collision occurred, process the hit and terminate the projectile.</li>
     * </ol>
     */
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