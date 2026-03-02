package org.dredd.bulletcore.models.weapons.shooting.projectile;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.compatibility.entity.FakeEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.bukkit.util.NumberConversions.square;

public abstract class AProjectile {

    // ----------< Static >----------

    private static final int CHECK_FOR_NEW_PLAYER_RATE = 40;

    public static final double NOT_USED = -1.0D;

    public static final double NO_GRAVITY = 0.0D;


    // ----------< Instance >----------

    // -----< Attributes >-----

    private final @Nullable Player shooter;
    private final @NotNull World world;
    private @Nullable FakeEntity disguise;
    private int lastDisguiseUpdateTick;
    private @NotNull Vector location;
    private @NotNull Vector motion;
    private double motionLength;
    private int aliveTicks;
    private double distanceTravelled;
    private boolean dead;


    // -----< Construction >-----

    protected AProjectile(@Nullable Player shooter,
                          @NotNull Location location,
                          @NotNull Vector motion) {
        this.shooter = shooter;
        this.world = location.getWorld();
        this.disguise = null;
        this.lastDisguiseUpdateTick = 0; // with 0 the disguise will NOT be updated after first updatePosition(), is this intended?
        this.location = location.toVector();
        this.motion = motion;
        this.motionLength = motion.length();
        this.aliveTicks = 0; // is incremented after updateDisguise()
        this.distanceTravelled = 0.0D;
        this.dead = false;
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


    // -----< Getters >-----

    public final @Nullable Player getShooter() {return shooter;}

    public final @NotNull World getWorld() {return world;}

    public @NotNull Vector getLocation() {return location.clone();}

    public @NotNull Block getCurrentBlock() {
        return world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public @NotNull Vector getMotion() {return motion.clone();}

    public double getMotionLength() {return motionLength;}

    public @NotNull Vector getNormalizedMotion() {
        final Vector motion = getMotion();
        return motionLength == 0.0D
            ? motion
            : motion.multiply(1.0D / motionLength);
    }

    public int getAliveTicks() {return aliveTicks;}

    public double getDistanceTravelled() {return distanceTravelled;}


    // -----< Setters >-----

    public void setRawLocation(@NotNull Vector location) {this.location = location;}

    public void setMotion(@NotNull Vector motion) {
        this.motion = motion;
        this.motionLength = motion.length();
    }


    // -----< Behavior >-----

    /** @return true if projectile should be removed */
    public boolean tick() {
        if (dead) return true;

        // Update motion BEFORE updating position, see #339
        final double gravity = getGravity();
        if (gravity != NO_GRAVITY) motion.setY(motion.getY() - gravity);
        motion.multiply(getDrag());

        // Handle collisions, will update location and distance traveled
        if (updatePosition()) return true;

        // If lived max ticks or is out of world in Y direction, remove
        final double locationY = location.getY();
        if (aliveTicks >= getMaximumAliveTicks() ||
            locationY < world.getMinHeight() || locationY > world.getMaxHeight()) {
            return true;
        }

        // No gravity and no motion -> update using teleport packet (why?)
        if (gravity == NO_GRAVITY && motionLength < Vector.getEpsilon()) {
            motionLength = 0;
            updateDisguise(true);
            aliveTicks++;
            return false;
        }

        motionLength = motion.length();

        final double minSpeed = getMinSpeed();
        final double maxSpeed = getMaxSpeed();
        if (minSpeed != NOT_USED && motionLength < minSpeed) {
            if (doRemoveAtMinSpeed()) return true;
            setMotion(getNormalizedMotion().multiply(minSpeed));
        } else if (maxSpeed != NOT_USED && motionLength > maxSpeed) {
            if (doRemoveAtMaxSpeed()) return true;
            setMotion(getNormalizedMotion().multiply(maxSpeed));
        }

        updateDisguise(false);
        aliveTicks++;
        return false;
    }

    /**
     * Spawn or ignore if this projectile already spawned a disguise.
     */
    public final void spawnDisguise(@Nullable FakeEntity fakeEntity) {
        if (fakeEntity == null || disguise != null) return;
        this.disguise = fakeEntity;
        fakeEntity.show();
    }

    /**
     * Must not be called multiple times on the same tick.
     *
     * @param useTeleport true to use teleport packet
     */
    protected void updateDisguise(boolean useTeleport) {
        if (disguise == null || lastDisguiseUpdateTick == aliveTicks) return;

        // Show for new players in range
        if (aliveTicks % CHECK_FOR_NEW_PLAYER_RATE == 0) disguise.show();

        if (motionLength == 0.0D) {
            disguise.setPosition(location.getX(), location.getY(), location.getZ(), disguise.getYaw(), disguise.getPitch(), useTeleport);
        } else {
            final Vector normalizedMotion = getNormalizedMotion();
            disguise.setPosition(location.getX(), location.getY(), location.getZ(), calculateYaw(normalizedMotion), calculatePitch(normalizedMotion), useTeleport);
        }

        lastDisguiseUpdateTick = aliveTicks;
    }

    private float calculateYaw(@NotNull Vector normalizedMotion) {
        return (float) Math.toDegrees((Math.atan2(-normalizedMotion.getX(), normalizedMotion.getZ()) + Math.TAU) % Math.TAU);
    }

    private float calculatePitch(@NotNull Vector normalizedMotion) {
        final double horizontalDistance = Math.sqrt(square(normalizedMotion.getX()) + square(normalizedMotion.getZ()));
        return (float) Math.toDegrees(Math.atan(-normalizedMotion.getY() / horizontalDistance));
    }

    /**
     * Must update the projectile's position/velocity, handle physical interactions
     * during that movement and update {@link #distanceTravelled} using {@link #addDistanceTravelled}.
     *
     * @return true if projectile should be removed
     */
    public abstract boolean updatePosition();

    public final void addDistanceTravelled(double amount) {this.distanceTravelled += amount;}

    /**
     * Marks projectile for removal and will be removed on this or next tick.
     */
    public void remove() {
        if (dead) return;
        this.dead = true;

        updateDisguise(true);

        if (disguise != null) disguise.remove();
    }
}