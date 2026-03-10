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

    private static final int SHOW_DISGUISE_FOR_NEW_PLAYERS_RATE = 20;

    public static final double NOT_USED = -1.0D;


    // ----------< Instance >----------

    // -----< Attributes >-----

    private final World world;
    private final Location location;
    private final Vector velocity;
    private double traveledDistance;

    private final @Nullable FakeEntity disguise;
    private int disguiseLastUpdateTick;
    private int aliveTicks;
    private boolean removed;


    // -----< Construction >-----

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


    // -----< Settings >-----

    protected double getGravity() {return 0.05D;}

    protected double getMinSpeed() {return NOT_USED;}

    protected boolean doRemoveAtMinSpeed() {return false;}

    protected double getMaxSpeed() {return NOT_USED;}

    protected boolean doRemoveAtMaxSpeed() {return false;}

    protected double getDrag() {
        if (getCurrentBlock().isLiquid()) return 0.96D;
        if (world.isThundering() || world.hasStorm()) return 0.98D;
        return 0.99D;
    }

    protected int getMaximumAliveTicks() {return 600;}

    protected double getMaxDistance() {return 1_000.0D;}


    // -----< Getters >-----

    protected final @NotNull World getWorld() {return world;}

    protected final @NotNull Block getCurrentBlock() {return location.getBlock();}


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

        final Location location = this.location;
        final World world = this.world;
        final double locationY = location.getY();
        if (locationY < world.getMinHeight() || locationY > world.getMaxHeight()) return true;
        if (!location.isChunkLoaded()) return true;

        // 2. Update velocity (gravity + drag)
        final Vector velocity = this.velocity;
        velocity.setY(velocity.getY() - getGravity());
        velocity.multiply(getDrag());

        double velocityLength = velocity.length();

        // 3. Check min/max speed
        final double minSpeed = getMinSpeed();
        final double maxSpeed = getMaxSpeed();
        double targetSpeed = velocityLength;

        if (minSpeed != NOT_USED && velocityLength < minSpeed) {
            if (doRemoveAtMinSpeed()) return true;
            targetSpeed = minSpeed;
        } else if (maxSpeed != NOT_USED && velocityLength > maxSpeed) {
            if (doRemoveAtMaxSpeed()) return true;
            targetSpeed = maxSpeed;
        }

        if (targetSpeed != velocityLength) {
            velocity.multiply(targetSpeed / velocityLength);
            velocityLength = targetSpeed;
        }

        // 4. Check if there is still velocity
        if (velocityLength < 1.0E-6) {
            velocity.zero();
            updateDisguise();
            aliveTicks++;
            return false;
        }

        // 5. Compute move distance, clamped by maximum remaining range
        final double maxDistance = getMaxDistance();
        final double remainingDistance = maxDistance - traveledDistance;
        final double moveDistance = Math.min(velocityLength, remainingDistance);

        // 6. Ray trace for collision detection
        if (handleCollisions(location, velocity, moveDistance)) return true;

        // 7. Update traveled distance
        this.traveledDistance += moveDistance;
        if (traveledDistance >= maxDistance) {
            // no real benefit to update the final location
            // (the projectile will be removed immediately)
            return true;
        }

        // 8. Update position
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
     * Updates the disguise entity, showing it for new players or just updating its position.
     */
    private void updateDisguise() {
        final FakeEntity disguise = this.disguise;
        if (disguise == null) return;

        final int aliveTicks = this.aliveTicks;
        if (aliveTicks == disguiseLastUpdateTick) return;

        if (aliveTicks % SHOW_DISGUISE_FOR_NEW_PLAYERS_RATE == 0)
            disguise.show();

        final Location l = location;
        disguise.setPosition(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());

        this.disguiseLastUpdateTick = aliveTicks;
    }

    /**
     * Performs the ray trace for collision detection.
     *
     * @param location     the current projectile location
     * @param velocity     the current projectile velocity
     * @param moveDistance the distance that the projectile is able to move this tick
     * @return {@code true} if projectile collided so that it should be removed, {@code false} to keep it alive
     */
    protected abstract boolean handleCollisions(@NotNull Location location,
                                                @NotNull Vector velocity,
                                                double moveDistance);

    /**
     * Marks the projectile as removed and removes the disguise entity if it exists.
     */
    public void remove() {
        if (removed) return;
        this.removed = true;

        final FakeEntity disguise = this.disguise;
        if (disguise != null) disguise.remove();
    }
}