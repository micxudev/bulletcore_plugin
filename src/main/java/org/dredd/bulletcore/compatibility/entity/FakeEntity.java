package org.dredd.bulletcore.compatibility.entity;

import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a packet based {@link org.bukkit.entity.Entity} with no server functions. Faked entities
 * are not ticked, rendered, moved, or in any other way "handled" by the server.
 * <p>
 * Fake entities are usually used for visual effects, since faked entities can control appearances
 * per player. After changing a visual effect (metadata + display name + gravity + etc), a metadata
 * packet must be sent using {@link #updateMeta()}.
 */
public abstract class FakeEntity {

    // -----< Attributes >-----

    protected final @NotNull EntityType type;
    protected final @NotNull Location location;


    // -----< Construction >-----

    public FakeEntity(@NotNull EntityType type, World world) {
        Objects.requireNonNull(world, "World cannot be null");
        this.type = type;
        this.location = new Location(world, 0, 0, 0);
    }


    // -----< Entity Meta >-----

    public abstract boolean getMeta(@NotNull EntityMetaFlag flag);

    public abstract void setMeta(@NotNull EntityMetaFlag flag, boolean enabled);

    /**
     * Updates the meta for all players that currently see this entity.<br>
     * This method should be called after any modifications to entity meta.
     */
    public abstract void updateMeta();


    // -----< Location >-----

    public final float getYaw() {return location.getYaw();}

    public final float getPitch() {return location.getPitch();}

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
     * @param x The new position on the x-axis.
     * @param y The new position on the y-axis.
     * @param z The new position on the z-axis.
     * @param yaw The yaw to set the entity at.
     * @param pitch The pitch to set the entity at.
     */
    public final void setPosition(double x, double y, double z, float yaw, float pitch) {
        setLocation(x, y, z, yaw, pitch);
        sendTeleportPacket(yaw);
        if (type == EntityType.ARMOR_STAND) updateMeta();
    }

    /**
     * Converts (yaw/pitch) degrees as float into byte<br>
     * (protocol uses byte for sending yaw/pitch)<br>
     * (server packs float to byte, client unpacks byte to float back)
     */
    protected final byte convertToByte(float degrees) {
        return (byte) (degrees * 256.0F / 360.0F);
    }

    /**
     * Sends a Teleport Entity Packet to all players who can see this entity.
     * @param yaw The absolute yaw rotation of the entity.
     */
    protected abstract void sendTeleportPacket(float yaw);


    // -----< Visibility >-----

    /**
     * Shows this entity to all players within the view server distance from the entity's location.
     * <p>
     * Sends an Add Entity Packet and a Set Entity Data Packet.
     */
    public abstract void show();

    /**
     * Hides the entity for all players that currently see it.
     * <p>
     * Sends a Remove Entities Packet.
     */
    public abstract void remove();
}