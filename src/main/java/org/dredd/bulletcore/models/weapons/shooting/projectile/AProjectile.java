package org.dredd.bulletcore.models.weapons.shooting.projectile;

import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.compatibility.entity.FakeEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AProjectile {

    // ----------< Static >----------

    private static final int CHECK_FOR_NEW_PLAYER_RATE = 20;

    public static final double NOT_USED = -1.0D;

    public static final double NO_GRAVITY = 0.0D;


    // ----------< Instance >----------

    // -----< Attributes >-----

    private final World world;
    private final Location currentLocation;
    private final Vector motion;
    private double motionLength;
    private double traveledDistance;

    private final @Nullable FakeEntity disguise;
    private int lastDisguiseUpdateTick;
    private int aliveTicks;
    private boolean removed;


    // -----< Construction >-----

    protected AProjectile(@NotNull Location location,
                          @NotNull Vector motion,
                          @Nullable FakeEntity disguise) {
        final World world = location.getWorld();
        Objects.requireNonNull(world, "World cannot be null");
        this.world = world;
        this.currentLocation = location.clone();
        this.motion = motion.clone();
        this.motionLength = motion.length();
        this.traveledDistance = 0.0D;

        this.disguise = disguise;
        this.lastDisguiseUpdateTick = -1;
        this.aliveTicks = 0;
        this.removed = false;
    }


    // -----< Settings >-----

    public double getGravity() {return 0.05D;}

    public double getMinSpeed() {return NOT_USED;}

    public boolean doRemoveAtMinSpeed() {return false;}

    public double getMaxSpeed() {return NOT_USED;}

    public boolean doRemoveAtMaxSpeed() {return false;}

    public double getDrag() {
        if (getCurrentBlock().isLiquid()) return 0.96D;
        if (world.isThundering() || world.hasStorm()) return 0.98D;
        return 0.99D;
    }

    public int getMaximumAliveTicks() {return 600;}

    public double getMaxDistance() {return 1_000_000.0D;}


    // -----< Getters >-----

    public final @NotNull World getWorld() {return world;}

    public @NotNull Block getCurrentBlock() {return currentLocation.getBlock();}


    // -----< Behavior >-----

    /**
     * Performs a single tick of the projectile's movement.
     *
     * @return {@code true} if projectile should be removed, {@code false} to keep it alive
     */
    public boolean tick() {
        // 1. Early validity checks
        if (removed) return true;
        if (aliveTicks >= getMaximumAliveTicks()) return true;

        final Location location = currentLocation;
        final World world = this.world;
        final double locationY = location.getY();
        if (locationY < world.getMinHeight() || locationY > world.getMaxHeight()) return true;
        if (!location.isChunkLoaded()) return true;

        // 2. Update motion (gravity + drag)
        final double gravity = getGravity();
        final Vector velocity = motion;
        if (gravity != NO_GRAVITY) velocity.setY(velocity.getY() - gravity);
        final double drag = getDrag();
        velocity.multiply(drag); // TODO 0. NOT NOW (change definition + application of drag)
        this.motionLength *= drag;

        // 3. Check min/max speed
        final double minSpeed = getMinSpeed();
        final double maxSpeed = getMaxSpeed();
        if (minSpeed != NOT_USED && motionLength < minSpeed) {
            // minSpeed IS used AND current velocity is slower than the minimum
            if (doRemoveAtMinSpeed()) return true;
            // increase to the minimum speed
            // TODO 1. is this correct and optimal to set the minimum speed?
            velocity.normalize().multiply(minSpeed);
            this.motionLength = minSpeed;
        } else if (maxSpeed != NOT_USED && motionLength > maxSpeed) {
            // maxSpeed IS used AND current velocity is faster than the maximum
            if (doRemoveAtMaxSpeed()) return true;
            // decrease to the maximum speed
            // TODO 2. is this correct and optimal to set the maximum speed?
            velocity.normalize().multiply(maxSpeed);
            this.motionLength = maxSpeed;
        }

        // 4. Check if there is still motion
        if (motionLength < 1.0E-6) {
            velocity.zero();
            this.motionLength = 0.0D;
            updateDisguise();
            aliveTicks++;
            return false;
        }

        // 5. Compute move distance, clamped by maximum remaining range
        final double maxDistance = getMaxDistance();
        final double remainingDistance = maxDistance - traveledDistance;
        final double moveDistance = Math.min(motionLength, remainingDistance);

        // 6. Ray trace for collision detection
        if (handleCollisions(location, velocity, moveDistance)) return true;

        // 7. Update traveled distance
        this.traveledDistance += moveDistance;
        if (traveledDistance >= maxDistance) {
            // ...
            // if we are here it means we most probably
            // did not move by moveDistance, so the
            // final location should be calculated...
            // and then 'updateDisguise(false)' can be called to notify the disguise update
            // but right after that we return true and this
            // disguise will be removed.
            // Do we even have to do this?
            // ...
            return true;
        }

        // TODO 4. Since we get here distanceTravelled < maxDistance -> position can be updated fully by motion, right???

        // 8. Update position
        location.add(velocity);
        location.setDirection(velocity);

        updateDisguise();
        aliveTicks++;
        return false;
    }

    /**
     * Updates the disguise entity, showing it for new players or just updating its position.
     */
    private void updateDisguise() {
        final FakeEntity disguise = this.disguise;
        if (disguise == null) return;

        final int aliveTicks = this.aliveTicks;
        if (aliveTicks == lastDisguiseUpdateTick) return;

        // Show for new players in range
        if (aliveTicks % CHECK_FOR_NEW_PLAYER_RATE == 0)
            disguise.show();

        final Location l = currentLocation;
        disguise.setPosition(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());

        this.lastDisguiseUpdateTick = aliveTicks;
    }

    /**
     * Performs the ray trace for collision detection.
     *
     * @param location     the current projectile location
     * @param velocity     the current projectile velocity
     * @param moveDistance the distance that the projectile is able to move this tick
     * @return {@code true} if projectile collided so that it should be removed, {@code false} to keep it alive
     */
    public abstract boolean handleCollisions(@NotNull Location location,
                                             @NotNull Vector velocity,
                                             double moveDistance);

    /**
     * Marks the projectile as removed and removes the disguise entity if it exists.
     */
    public void remove() {
        if (removed) return;
        this.removed = true;

        if (disguise != null) disguise.remove();
    }
}