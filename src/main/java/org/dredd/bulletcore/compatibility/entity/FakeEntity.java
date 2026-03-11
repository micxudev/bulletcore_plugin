package org.dredd.bulletcore.compatibility.entity;

import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a packet based {@link Entity} with no server functions.
 * <p>
 * Fake entities are not ticked, moved, or in any other way "handled" by the server.
 * <p>
 * Fake entities are usually used for visual effects.
 * <p>
 * After changing a metadata using {@link #setMeta(EntityMetaFlag, boolean)}, a metadata
 * packet must be sent using {@link #updateMeta()}.
 *
 * @since 1.0.0
 */
public abstract class FakeEntity {

    // -----< Attributes >-----

    protected final @NotNull EntityType type;
    protected final @NotNull Location location;


    // -----< Construction >-----

    protected FakeEntity(@NotNull EntityType type, World world) {
        Objects.requireNonNull(world, "World cannot be null");
        this.type = type;
        this.location = new Location(world, 0, 0, 0);
    }


    // -----< Entity Meta >-----

    /**
     * Retrieves the value for the given meta flag.
     *
     * @param flag the flag to check
     * @return {@code true} if the flag is enabled, {@code false} otherwise
     */
    public abstract boolean getMeta(@NotNull EntityMetaFlag flag);

    /**
     * Sets the value for the given meta flag.
     *
     * @param flag the flag to set
     * @param enabled the new value for the flag, {@code true} to enable, {@code false} to disable
     */
    public abstract void setMeta(@NotNull EntityMetaFlag flag, boolean enabled);

    /**
     * Updates the meta for all players that currently see this entity.
     * <p>
     * Sends a {@code Set Entity Data Packet}.
     */
    public abstract void updateMeta();


    // -----< Location >-----

    protected final float getYaw() {return location.getYaw();}

    protected final float getPitch() {return location.getPitch();}

    protected void setLocation(double x, double y, double z, float yaw, float pitch) {
        location.setX(x);
        location.setY(y);
        location.setZ(z);
        location.setYaw(yaw);
        location.setPitch(pitch);
    }

    /**
     * Sets the new position of this entity.
     *
     * @param x The new position on the x-axis
     * @param y The new position on the y-axis
     * @param z The new position on the z-axis
     * @param yaw The yaw to set the entity at
     * @param pitch The pitch to set the entity at
     */
    public final void setPosition(double x, double y, double z, float yaw, float pitch) {
        setLocation(x, y, z, yaw, pitch);
        sendTeleportPacket(yaw);
        if (type == EntityType.ARMOR_STAND) updateMeta();
    }

    /**
     * Converts (yaw/pitch) degrees as float into byte.<br>
     * Protocol uses byte for sending yaw/pitch.<br>
     * Server packs float to byte, client unpacks byte to float back
     */
    protected final byte convertToByte(float degrees) {
        return (byte) (degrees * 256.0F / 360.0F);
    }

    /**
     * Teleports the entity to the currently set position.
     * <p>
     * Sends a {@code Teleport Entity Packet}, {@code Rotate Head Packet}.
     *
     * @param yaw The absolute yaw rotation of the entity.
     */
    protected abstract void sendTeleportPacket(float yaw);


    // -----< Visibility >-----

    /**
     * Shows this entity to all players within the view server distance from the entity's location.
     * <p>
     * Sends an {@code Add Entity Packet}, {@code Set Entity Data Packet}.<br>
     * And a {@code Set Equipment Packet} (only for {@link EntityType#ARMOR_STAND}
     * if it has a non-empty item on a head).
     */
    public abstract void show();

    /**
     * Hides the entity for all players that currently see it.
     * <p>
     * Sends a {@code Remove Entities Packet}.
     */
    public abstract void remove();
}