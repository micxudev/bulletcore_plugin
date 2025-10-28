package org.dredd.bulletcore.armorstand_features.features;

import io.papermc.paper.math.Rotations;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.dredd.bulletcore.armorstand_features.ArmorStandHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Visual feature that renders a bullet hole where a bullet hits a block.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class BulletHoleFeature extends ArmorStandFeature {

    // ----------< Static >----------

    // -----< Defaults >-----

    /**
     * The default material used for this feature.
     */
    private static final Material DEFAULT_MATERIAL = Material.IRON_NUGGET;

    /**
     * The default number of ticks after which the bullet hole is removed (10 seconds).
     */
    private static final long DEFAULT_REMOVE_AFTER_TICKS = 200L;

    /**
     * The minimum number of ticks after which a bullet hole is removed (1 second).
     */
    private static final long MIN_REMOVE_AFTER_TICKS = 20L;

    /**
     * The maximum number of ticks after which a bullet hole is removed (1 hour).
     */
    private static final long MAX_REMOVE_AFTER_TICKS = 20L * 60L * 60L;

    // -----< Position offsets (derived experimentally) >-----

    private static final double VERTICAL_OFFSET = 1.2563;
    private static final double BACK_OFFSET = 0.19;
    private static final double HORIZONTAL_OFFSET = 0.53;

    // -----< Head rotations >-----

    private static final Rotations HEAD_ROT_UP = Rotations.ofDegrees(90, 0, 0);
    private static final Rotations HEAD_ROT_DOWN = Rotations.ofDegrees(-90, 0, 0);
    private static final Rotations HEAD_ROT_SOUTH = Rotations.ofDegrees(0, 180, 0);
    private static final Rotations HEAD_ROT_WEST = Rotations.ofDegrees(0, -90, 0);
    private static final Rotations HEAD_ROT_EAST = Rotations.ofDegrees(0, 90, 0);
    private static final Rotations HEAD_ROT_NORTH = Rotations.ofDegrees(0, 0, 0);

    // -----< Loader >-----

    /**
     * Loads a {@code BulletHoleFeature} from a config section, or defaults if the section is null.
     *
     * @param section the configuration section to load from
     * @return a new {@link BulletHoleFeature} instance
     */
    public static @NotNull BulletHoleFeature load(@Nullable ConfigurationSection section) {
        return (section == null)
            ? new BulletHoleFeature()
            : new BulletHoleFeature(section);
    }


    // ----------< Instance >----------

    // -----< Attributes >-----

    /**
     * The number of ticks after which the spawned armor stand is automatically removed.
     */
    private final long removeAfterTicks;

    // -----< Construction >-----

    private BulletHoleFeature() {
        super(DEFAULT_MATERIAL);

        this.removeAfterTicks = DEFAULT_REMOVE_AFTER_TICKS;
    }

    private BulletHoleFeature(@NotNull ConfigurationSection section) {
        super(section, DEFAULT_MATERIAL);

        this.removeAfterTicks = Math.clamp(
            section.getLong("removeAfter", DEFAULT_REMOVE_AFTER_TICKS),
            MIN_REMOVE_AFTER_TICKS, MAX_REMOVE_AFTER_TICKS
        );
    }

    // -----< API >-----

    /**
     * Spawns a bullet hole at the given block hit location.<br>
     * The armor stand's position and rotation are adjusted to match the block face.
     *
     * @param world        the world to spawn in
     * @param hitLocation  the location where the bullet hit
     * @param hitBlockFace the face of the block that was hit
     */
    public void spawn(@NotNull World world,
                      @NotNull Location hitLocation,
                      @NotNull BlockFace hitBlockFace) {
        if (!enabled) return;

        Location spawnLoc = hitLocation.clone()
            .subtract(0, VERTICAL_OFFSET, 0)
            .subtract(hitBlockFace.getDirection().multiply(BACK_OFFSET));

        switch (hitBlockFace) {
            case UP -> spawnLoc.add(0, HORIZONTAL_OFFSET, -HORIZONTAL_OFFSET);
            case DOWN -> spawnLoc.add(0, HORIZONTAL_OFFSET, HORIZONTAL_OFFSET);
        }

        ArmorStand stand = ArmorStandHandler.spawn(world, spawnLoc, item, mapFaceToRotation(hitBlockFace));
        ArmorStandHandler.scheduleRemoval(stand, removeAfterTicks);
    }

    // -----< Utilities >-----

    /**
     * Maps a block face to the head rotation.
     *
     * @param face the block face to align with
     * @return the corresponding {@link Rotations} for the ArmorStand's head
     */
    private @NotNull Rotations mapFaceToRotation(@NotNull BlockFace face) {
        return switch (face) {
            case UP -> HEAD_ROT_UP;
            case DOWN -> HEAD_ROT_DOWN;
            case SOUTH -> HEAD_ROT_SOUTH;
            case WEST -> HEAD_ROT_WEST;
            case EAST -> HEAD_ROT_EAST;
            default -> HEAD_ROT_NORTH;
        };
    }
}