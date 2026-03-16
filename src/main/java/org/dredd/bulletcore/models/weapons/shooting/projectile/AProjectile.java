package org.dredd.bulletcore.models.weapons.shooting.projectile;

import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.compatibility.entity.FakeEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for all projectile implementations.
 * <p>
 * This class provides the core simulation logic for projectiles, including:
 * <ul>
 *     <li>velocity and gravity updates</li>
 *     <li>drag simulation</li>
 *     <li>speed clamping</li>
 *     <li>distance and lifetime limits</li>
 *     <li>visual disguise synchronization</li>
 * </ul>
 *
 * Subclasses are responsible for implementing collision handling by
 * overriding {@link #handleCollisions(Location, Vector, double)}.
 *
 * @author dredd
 * @since 1.0.0
 */
public abstract class AProjectile {

    // ----------< Static >----------

    /**
     * Interval (in ticks) at which the disguise entity is re-shown to players.
     * <p>
     * This ensures the projectile remains visible to players who were not
     * previously tracking it, for example:
     * <ul>
     *     <li>players who joined the server after the projectile spawned</li>
     *     <li>players who moved into render distance</li>
     *     <li>players who teleported nearby</li>
     * </ul>
     */
    private static final int DISGUISE_RESYNC_RATE = 20;

    /**
     * Special value used to indicate that a feature is disabled or not used.
     */
    public static final double NOT_USED = -1.0D;

    /**
     * Minimum speed threshold below which the projectile is considered stationary.
     * <p>
     * When the speed drops below this threshold, the velocity is set to zero
     * to avoid unnecessary movement calculations and collision checks.
     * <p>
     * Value explanation:
     * <ul>
     *     <li>{@code 0.005 blocks/tick}</li>
     *     <li>≈ {@code 0.1 blocks/second}</li>
     *     <li>≈ {@code 0.1 meters/second}</li>
     * </ul>
     *
     * This is slow enough that the projectile appears visually stationary.
     */
    private static final double MIN_MOVEMENT_SPEED = 5.0E-3;


    // ----------< Instance >----------

    // -----< Attributes >-----

    /**
     * The world in which the projectile exists.
     * <p>
     * Stored separately to:
     * <ul>
     *     <li>avoid repeated {@link Location#getWorld()} calls</li>
     *     <li>ensure a stable world reference during the projectile lifetime</li>
     * </ul>
     */
    private final World world;

    /**
     * Current position of the projectile.
     * <p>
     * This location is mutated every tick as the projectile moves.
     */
    private final Location location;

    /**
     * Current velocity vector of the projectile (blocks per tick).
     * <p>
     * The velocity is modified each tick by gravity, drag, and speed limits.
     */
    private final Vector velocity;

    /**
     * Total distance traveled by the projectile in blocks.
     */
    private double traveledDistance;


    /**
     * Optional fake entity used to visually represent the projectile to players.
     * <p>
     * The projectile itself is purely simulated and does not exist as a real Minecraft entity.
     */
    private final @Nullable FakeEntity disguise;

    /**
     * The last tick at which the disguise position was updated.
     * <p>
     * Used to prevent multiple updates within the same tick.
     */
    private int disguiseLastUpdateTick;

    /**
     * Number of ticks the projectile has existed.
     */
    private int aliveTicks;

    /**
     * Indicates whether the projectile has been removed.
     * <p>
     * Once removed, the projectile will no longer tick.
     */
    private boolean removed;


    // -----< Construction >-----

    /**
     * Creates a new projectile instance.
     *
     * @param location initial projectile location
     * @param velocity initial projectile velocity (blocks per tick)
     * @param disguise optional visual representation of the projectile
     */
    protected AProjectile(@NotNull Location location,
                          @NotNull Vector velocity,
                          @Nullable FakeEntity disguise) {
        final World world = location.getWorld();
        Objects.requireNonNull(world, "World cannot be null");
        this.world = world;
        this.location = location.clone();
        this.velocity = velocity.clone();

        this.disguise = disguise;
        this.disguiseLastUpdateTick = -1;
    }


    // -----< Physics Settings >-----

    /**
     * Returns the gravitational acceleration applied each tick.
     *
     * @return downward acceleration in blocks per tick²
     */
    protected double getGravity() {return 0.05D;}

    /**
     * Returns the minimum allowed projectile speed.
     * <p>
     * If the projectile speed drops below this value, it will be clamped to it.
     *
     * @return minimum speed in blocks per tick, or {@value #NOT_USED} to disable this constraint
     */
    protected double getMinSpeed() {return NOT_USED;}

    /**
     * Determines whether the projectile should be removed when its speed
     * reaches the minimum allowed speed.
     *
     * @return {@code true} to remove the projectile at minimum speed
     */
    protected boolean removeWhenMinSpeedReached() {return false;}

    /**
     * Returns the maximum allowed projectile speed.
     * <p>
     * If the projectile speed exceeds this value, it will be clamped to it.
     *
     * @return maximum speed in blocks per tick, or {@value #NOT_USED} to disable this constraint
     */
    protected double getMaxSpeed() {return NOT_USED;}

    /**
     * Determines whether the projectile should be removed when its speed
     * reaches the maximum allowed speed.
     *
     * @return {@code true} to remove the projectile at maximum speed
     */
    protected boolean removeWhenMaxSpeedReached() {return false;}

    /**
     * Returns a drag coefficient applied to the projectile's velocity each tick.
     * <p>
     * Values:
     * <ul>
     *     <li>{@code 1.0} – no drag (velocity unchanged)</li>
     *     <li>{@code < 1.0} – slows the projectile</li>
     *     <li>{@code > 1.0} – speeds up the projectile</li>
     * </ul>
     *
     * The default implementation applies additional drag when the projectile
     * is inside liquids or when the world is experiencing a storm.
     */
    protected double getDrag() {
        if (getCurrentBlock().isLiquid())
            return 0.9D;

        final World world = this.world;
        if (world.isThundering() || world.hasStorm())
            return 0.96;

        return 1.0D;
    }

    /**
     * Maximum lifetime of the projectile.
     *
     * @return lifetime in ticks (20 ticks ≈ 1 second)
     */
    protected int getMaximumAliveTicks() {return 600;}

    /**
     * Maximum distance the projectile may travel.
     *
     * @return distance in blocks before the projectile is removed
     */
    protected double getMaxDistance() {return 1_000.0D;}


    // -----< Getters >-----

    /**
     * Returns the world in which the projectile exists.
     * <p>
     * This is the same world in which the projectile was spawned and where
     * all its simulation and collision checks are performed.
     *
     * @return the projectile's world
     */
    protected final @NotNull World getWorld() {return world;}

    /**
     * Returns the block currently containing the projectile.
     *
     * @return the block at the projectile's current location
     */
    protected final @NotNull Block getCurrentBlock() {return location.getBlock();}


    // -----< Behavior >-----

    /**
     * Advances the projectile simulation by one server tick (~20 times per second).
     * <p>
     * This method performs the full projectile update cycle, including physics,
     * movement, collision detection, and visual synchronization.
     *
     * <p><b>Tick pipeline:</b>
     * <ol>
     *     <li>Validate the projectile state (removal flag, lifetime, world bounds).</li>
     *     <li>Update velocity by applying gravity and drag.</li>
     *     <li>Stop the projectile if its speed becomes tiny.</li>
     *     <li>Clamp the velocity to the configured minimum and maximum speeds.</li>
     *     <li>Clamp movement to the remaining allowed travel distance.</li>
     *     <li>Perform collision detection along the movement path.</li>
     *     <li>Update the total traveled distance.</li>
     *     <li>Move the projectile and update its visual representation.</li>
     * </ol>
     *
     * If any step determines that the projectile should stop existing
     * (for example due to collision, distance limit, or lifetime expiration),
     * the method returns {@code true}.
     *
     * @return {@code true} if the projectile should be removed,
     *         {@code false} if it should continue ticking
     */
    public boolean tick() {

        // 1. -----< Early validity checks >-----
        if (removed || aliveTicks >= getMaximumAliveTicks())
            return true;

        final Location location = this.location;
        final World world = this.world;

        final double y = location.getY();
        if (y < world.getMinHeight() || y > world.getMaxHeight() || !location.isChunkLoaded())
            return true;


        // 2. -----< Velocity update >-----
        final Vector velocity = this.velocity;

        velocity.setY(velocity.getY() - getGravity());
        velocity.multiply(getDrag());

        double speed = velocity.length();


        // 3. -----< Stop nearly stationary projectiles >-----
        if (speed < MIN_MOVEMENT_SPEED) {
            velocity.zero();
            updateDisguise();
            aliveTicks++;
            return false;
        }


        // 4. -----< Clamp speed >-----
        final double minSpeed = getMinSpeed();
        final double maxSpeed = getMaxSpeed();

        final boolean isMinSpeedUsed = minSpeed != NOT_USED;
        final boolean isMaxSpeedUsed = maxSpeed != NOT_USED;

        if (isMinSpeedUsed || isMaxSpeedUsed) {

            double targetSpeed = speed;

            if (isMinSpeedUsed && speed < minSpeed) {
                if (removeWhenMinSpeedReached()) return true;
                targetSpeed = minSpeed;
            }

            if (isMaxSpeedUsed && speed > maxSpeed) {
                if (removeWhenMaxSpeedReached()) return true;
                targetSpeed = maxSpeed;
            }

            if (targetSpeed != speed) {
                velocity.multiply(targetSpeed / speed);
                speed = targetSpeed;
            }
        }


        // 5. -----< Distance clamp >-----
        final double maxDistance = getMaxDistance();
        final double remainingDistance = maxDistance - traveledDistance;
        final double moveDistance = Math.min(speed, remainingDistance);


        // 6. -----< Collision detection >-----
        if (handleCollisions(location, velocity, moveDistance))
            return true;


        // 7. -----< Distance update >-----
        if (moveDistance == remainingDistance) {
            // no real benefit to update the final location
            // (the projectile will be removed immediately)
            return true;
        }

        this.traveledDistance += moveDistance;


        // 8. -----< Move projectile >-----
        location.add(velocity);

        if (disguise != null) {
            // updates yaw and pitch
            // (only useful for disguise)
            // (no disguise => no update)
            location.setDirection(velocity);
        }

        updateDisguise();
        aliveTicks++;

        return false;
    }

    /**
     * Updates the disguise entity position and visibility.
     * <p>
     * The disguise position is synchronized with the projectile each tick.<br>
     * Additionally, the entity is periodically re-shown to ensure that players
     * not previously tracking it (for example due to render distance,
     * teleportation, or joining the server later) can see it.
     */
    private void updateDisguise() {
        final FakeEntity disguise = this.disguise;
        if (disguise == null) return;

        final int aliveTicks = this.aliveTicks;
        if (aliveTicks == disguiseLastUpdateTick) return;

        if (aliveTicks % DISGUISE_RESYNC_RATE == 0)
            disguise.show();

        final Location l = location;
        disguise.setPosition(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());

        this.disguiseLastUpdateTick = aliveTicks;
    }

    /**
     * Performs collision detection for the projectile's movement this tick.
     * <p>
     * This method is called before the projectile position is updated and is
     * responsible for detecting collisions along the projectile's movement path.
     * Implementations typically perform a ray trace starting at the current
     * location and extending in the direction of the projectile's velocity.
     * <p>
     * The ray length should not exceed {@code moveDistance}, which represents the
     * maximum distance the projectile is allowed to travel during this tick.
     * <p>
     * If a collision is detected, the implementation may perform custom logic
     * such as damaging entities, spawning effects, or modifying the projectile's
     * velocity. If the collision should terminate the projectile, this method
     * should return {@code true}.
     *
     * @param location     the current projectile location (start of the ray)
     * @param velocity     the current projectile velocity (movement direction when normalized)
     * @param moveDistance maximum distance the projectile may travel this tick
     *
     * @return {@code true} if the projectile collided and should be removed,
     * {@code false} to keep it ticking
     */
    protected abstract boolean handleCollisions(@NotNull Location location,
                                                @NotNull Vector velocity,
                                                double moveDistance);

    /**
     * Removes the projectile and its visual representation.
     * <p>
     * Once removed, the projectile will no longer tick.
     */
    public void remove() {
        if (removed) return;
        this.removed = true;

        final FakeEntity disguise = this.disguise;
        if (disguise != null) disguise.remove();
    }
}