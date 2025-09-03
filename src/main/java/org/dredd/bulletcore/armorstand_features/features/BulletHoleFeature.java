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
 * Visual feature that renders a bullet hole at the location where a bullet hits a block.
 *
 * @author dredd
 * @since 1.0.0
 */
public class BulletHoleFeature extends ArmorStandFeature {

    /**
     * The default material used for the bullet hole visual.
     */
    private static final Material DEF_MATERIAL = Material.IRON_NUGGET;

    /**
     * The default number of ticks after which the bullet hole is removed (10 seconds).
     */
    private static final long DEF_REMOVE_AFTER_TICKS = 200L;

    /**
     * The maximum number of ticks after which a bullet hole is removed (1 hour).
     */
    private static final long MAX_REMOVE_AFTER_TICKS = 20L * 60L * 60L;

    private static final double VERTICAL_OFFSET = 1.2563;
    private static final double BACK_OFFSET = 0.19;
    private static final double HORIZONTAL_OFFSET = 0.53;

    private static final Rotations HEAD_ROT_UP = Rotations.ofDegrees(90, 0, 0);
    private static final Rotations HEAD_ROT_DOWN = Rotations.ofDegrees(-90, 0, 0);
    private static final Rotations HEAD_ROT_SOUTH = Rotations.ofDegrees(0, 180, 0);
    private static final Rotations HEAD_ROT_WEST = Rotations.ofDegrees(0, -90, 0);
    private static final Rotations HEAD_ROT_EAST = Rotations.ofDegrees(0, 90, 0);
    private static final Rotations HEAD_ROT_NORTH = Rotations.ofDegrees(0, 0, 0);

    /**
     * The number of ticks after which the spawned armor stand is automatically removed.
     */
    private final long removeAfterTicks;

    /**
     * Constructs a new bullet hole feature with the given parameters.
     *
     * @param enabled          whether the feature is enabled
     * @param modelData        custom model data ({@value DEF_MODEL_DATA} = use default material)
     * @param removeAfterTicks number of ticks after which the feature is removed
     */
    private BulletHoleFeature(boolean enabled, int modelData, long removeAfterTicks) {
        super(enabled, DEF_MATERIAL, modelData);
        this.removeAfterTicks = removeAfterTicks;
    }

    /**
     * Spawns the bullet hole at the specified hit location and block face.
     * <p>
     * Adjusts the position and orientation of the armor stand based on the hit direction
     * to ensure it visually aligns with the surface of the block.
     *
     * @param world        the world in which to spawn the bullet hole
     * @param hitLocation  the location where the bullet hit
     * @param hitBlockFace the face of the block that was hit
     */
    public void spawn(@NotNull World world, @NotNull Location hitLocation, @NotNull BlockFace hitBlockFace) {
        if (!enabled) return;

        Location spawnLoc = hitLocation.clone()
            .subtract(0, VERTICAL_OFFSET, 0)
            .subtract(hitBlockFace.getDirection().multiply(BACK_OFFSET)); // move closer to the block face

        // Adjust position for vertical block faces
        if (hitBlockFace == BlockFace.UP)
            spawnLoc.add(0, HORIZONTAL_OFFSET, -HORIZONTAL_OFFSET);
        else if (hitBlockFace == BlockFace.DOWN)
            spawnLoc.add(0, HORIZONTAL_OFFSET, HORIZONTAL_OFFSET);

        ArmorStand stand = ArmorStandHandler.spawn(world, spawnLoc, item, getHeadRotationsForFace(hitBlockFace));
        ArmorStandHandler.scheduleRemoval(stand, removeAfterTicks);
    }

    /**
     * Returns the head rotation based on the given block face.
     *
     * @param face the block face to align with
     * @return the corresponding {@link Rotations} for the ArmorStand's head
     */
    private @NotNull Rotations getHeadRotationsForFace(@NotNull BlockFace face) {
        return switch (face) {
            case UP -> HEAD_ROT_UP;
            case DOWN -> HEAD_ROT_DOWN;
            case SOUTH -> HEAD_ROT_SOUTH;
            case WEST -> HEAD_ROT_WEST;
            case EAST -> HEAD_ROT_EAST;
            default -> HEAD_ROT_NORTH;
        };
    }

    /**
     * Loads a {@code BulletHoleFeature} instance from a configuration section.<br>
     * If the section is {@code null}, default values are used.
     *
     * @param section the configuration section to load from
     * @return a new {@code BulletHoleFeature} instance
     */
    public static @NotNull BulletHoleFeature load(@Nullable ConfigurationSection section) {
        if (section == null)
            return new BulletHoleFeature(DEF_ENABLED, DEF_MODEL_DATA, DEF_REMOVE_AFTER_TICKS);

        boolean enabled = section.getBoolean("enabled", DEF_ENABLED);
        int modelData = section.getInt("customModelData", DEF_MODEL_DATA);
        long removeAfterTicks = Math.clamp(section.getLong("removeAfter", DEF_REMOVE_AFTER_TICKS), 1L, MAX_REMOVE_AFTER_TICKS);

        return new BulletHoleFeature(enabled, modelData, removeAfterTicks);
    }
}