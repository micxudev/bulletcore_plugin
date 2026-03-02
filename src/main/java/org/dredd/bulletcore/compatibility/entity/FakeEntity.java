package org.dredd.bulletcore.compatibility.entity;

import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.bukkit.util.NumberConversions.square;

/**
 * Defines a packet based {@link org.bukkit.entity.Entity} with no server functions. Faked entities
 * are not ticked, rendered, moved, or in any other way "handled" by the server.
 *
 * <p>
 * Fake entities are usually used for visual effects, since faked entities can control appearances
 * per player. After changing a visual effect (metadata + display name + gravity + etc), a metadata
 * packet must be sent using {@link #updateMeta()}.
 */
public abstract class FakeEntity {

    // -----< Attributes >-----

    protected final @NotNull EntityType type;
    protected final @NotNull Location location;
    protected final @Nullable Location offset;


    // -----< Constructor >-----

    public FakeEntity(@NotNull EntityType type, World world) {
        Objects.requireNonNull(world, "World cannot be null");
        this.type = type;
        this.location = new Location(world, 0, 0, 0);
        this.offset = type != EntityType.ARMOR_STAND ? null : new Location(location.getWorld(), 0, -1.67875, 0);
    }


    // -----< Entity Meta >-----

    protected abstract boolean getMeta(@NotNull EntityMetaFlag flag);

    protected abstract void setMeta(@NotNull EntityMetaFlag flag, boolean enabled);

    /**
     * Updates the meta for all players that currently see it.<br>
     * This method should be called after any modifications to entity meta.
     */
    public abstract void updateMeta();


    // -----< Location >-----

    public float getYaw() {return location.getYaw();}

    public float getPitch() {return location.getPitch();}

    protected void setLocation(double x, double y, double z, float yaw, float pitch) {
        location.setX(x);
        location.setY(y);
        location.setZ(z);
        location.setYaw(yaw);
        location.setPitch(pitch);
    }

    /**
     * Sends an entity rotation packet to all players who can see this entity.
     * <p>
     * Implementing classes should set <code>this.location</code> using {@link Location#setYaw(float)}
     * and {@link Location#setPitch(float)}.
     *
     * @param yaw The absolute yaw rotation of the entity.
     * @param pitch The absolute pitch rotation of the entity.
     */
    public abstract void setRotation(float yaw, float pitch);

    /**
     * Sets position of this entity. When the new location is within 8 blocks, a move-look packet is
     * sent (using a relative position). Otherwise, a teleport packet is sent (using an absolute
     * position).
     *
     * <p>
     * If you do not want to change the entity's yaw/pitch, you may use {@link #getYaw()} and
     * {@link #getPitch()}.
     *
     * @param x The new position on the x-axis.
     * @param y The new position on the y-axis.
     * @param z The new position on the z-axis.
     * @param yaw The yaw to set the entity at.
     * @param pitch The pitch to set the entity at.
     * @param raw true to always use a teleport packet.
     */
    public final void setPosition(double x, double y, double z, float yaw, float pitch, boolean raw) {
        if (offset != null) {
            x += offset.getX();
            y += offset.getY();
            z += offset.getZ();
            yaw += offset.getYaw();
            pitch += offset.getPitch();
        }

        double lengthSquared = raw ? 0.0 : square(x - location.getX()) + square(y - location.getY()) + square(z - location.getZ());

        // When the change of position >8, then we cannot use the move-look
        // packet since it is limited by the size of a short. When we cannot
        // use move-look, we use a teleport packet instead.
        if (raw || lengthSquared == 0.0 || lengthSquared > 64.0) {
            setLocation(x, y, z, yaw, pitch);
            setPositionRaw(x, y, z, yaw, pitch);
        } else {
            setPositionRotation(x - location.getX(), y - location.getY(), z - location.getZ(), yaw, pitch);
            setLocation(x, y, z, yaw, pitch);
        }

        if (type == EntityType.ARMOR_STAND) updateMeta();
    }

    private void setPositionRotation(double dx, double dy, double dz, float yaw, float pitch) {
        setPositionRotation((short) (dx * 4096), (short) (dy * 4096), (short) (dz * 4096), convertYaw(yaw), convertPitch(pitch));
    }

    /**
     * Sends an entity move-look packet to all players who can see this entity. Effectively sets the
     * relative position of the entity.
     *
     * <p>
     * This method is protected to prevent accidental/improper usage.
     *
     * @param dx The change of position across the x-axis.
     * @param dy The change of position across the y-axis.
     * @param dz The change of position across the z-axis.
     * @param yaw The absolute yaw rotation of the entity.
     * @param pitch The absolute pitch rotation of the entity.
     */
    protected abstract void setPositionRotation(short dx, short dy, short dz, byte yaw, byte pitch);

    /**
     * Sends an entity teleport packet to all players who can see this entity. Effectively sets the
     * absolute position of the entity.
     *
     * <p>
     * This method is protected to prevent accidental/improper usage.
     *
     * @param x The absolute x position of the entity.
     * @param y The absolute y position of the entity.
     * @param z The absolute z position of the entity.
     * @param yaw The absolute yaw rotation of the entity.
     * @param pitch The absolute pitch rotation of the entity.
     */
    protected abstract void setPositionRaw(double x, double y, double z, float yaw, float pitch);

    protected final byte convertPitch(float degrees) {
        degrees *= 256.0f / 360.0f;
        if (!type.isAlive()) {
            return (byte) -degrees;
        }
        return (byte) degrees;
    }

    protected final byte convertYaw(float degrees) {
        degrees *= 256.0f / 360.0f;
        return switch (type) {
            case ARROW -> (byte) -degrees;
            case WITHER_SKULL, ENDER_DRAGON -> (byte) (degrees - 128.0f);
            default -> {
                if (!type.isAlive() && type != EntityType.ARMOR_STAND) {
                    yield (byte) (degrees - 64.0f);
                }
                yield (byte) degrees;
            }
        };
    }


    // -----< Visibility >-----

    /**
     * Shows this entity to all players within range of the entity.
     * <p>
     * Sends an Add Entity Packet and a Set Entity Data Packet.
     */
    public abstract void show();

    /**
     * Shows the entity to the given player.
     * <p>
     * Sends an Add Entity Packet and a Set Entity Data Packet.
     *
     * @param player The player to show the entity to.
     */
    public abstract void show(@NotNull Player player);

    /**
     * Hides the entity for all players that currently see it.
     * <p>
     * Sends an Remove Entities Packet.
     * <p>
     * Players will no longer be able to see the entity
     * (unless they are added back using {@link #show(Player)}).
     */
    public abstract void remove();

    /**
     * Hides the entity for the given player.
     * <p>
     * Sends an Remove Entities Packet.
     * <p>
     * The player will no longer be able to see the entity
     * (unless they are added back using {@link #show(Player)}).
     *
     * @param player The player to hide the entity from.
     */
    public abstract void remove(@NotNull Player player);


    // -----< Equipment >-----

    /**
     * Sets new item to given equipment slot.
     *
     * @param equipmentSlot the equipment slot to modify
     * @param itemStack the item stack set to slot
     */
    public abstract void setEquipment(@NotNull EquipmentSlot equipmentSlot, @Nullable ItemStack itemStack);

    /**
     * Updates the equipment for all players that currently see it.<br>
     * This method should be called after calling {@link #setEquipment(EquipmentSlot, ItemStack)}.
     */
    public abstract void updateEquipment();


    // -----< Random Stuff >-----

    /**
     * Disables entity gravity. This has no effect server-side, and will not affect
     * motion/position/rotation/anything. Instead, this method tells the client that the entity should
     * not automatically have gravity applied. After calling this method use {@link #updateMeta()} to
     * show the information to clients.
     *
     * @param gravity true -> gravity, false -> no gravity.
     */
    public abstract void setGravity(boolean gravity);
}