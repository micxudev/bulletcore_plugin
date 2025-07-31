package org.dredd.bulletcore.armorstand_features.features;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;
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
     * Default material used for the bullet hole visual.
     */
    private static final Material DEF_MATERIAL = Material.IRON_NUGGET;

    /**
     * Default time (in ticks) after which the bullet hole is removed (200 ticks = 10 seconds).
     */
    private static final long DEF_REMOVE_AFTER_TICKS = 200L;

    private static final double VERTICAL_OFFSET = 1.2563;
    private static final double BACK_OFFSET = 0.19;
    private static final double HORIZONTAL_OFFSET = 0.53;

    /**
     * Time (in ticks) after which the spawned armor stand is automatically removed.
     */
    private final long removeAfterTicks;

    /**
     * Constructs a new bullet hole feature with the given parameters.
     *
     * @param enabled          whether the feature is enabled
     * @param modelData        custom model data (0 = use default material)
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

        ArmorStand stand = ArmorStandHandler.spawn(world, spawnLoc, item, getHeadPoseForBlockFace(hitBlockFace));
        ArmorStandHandler.scheduleRemoval(stand, removeAfterTicks);
    }

    /**
     * Gets the appropriate head pose for an armor stand based on the block face hit.
     *
     * @param face the block face that was hit
     * @return the corresponding EulerAngle for head rotation
     */
    private EulerAngle getHeadPoseForBlockFace(BlockFace face) {
        return switch (face) {
            case UP -> new EulerAngle(Math.toRadians(90), 0, 0);
            case DOWN -> new EulerAngle(Math.toRadians(-90), 0, 0);
            case SOUTH -> new EulerAngle(0, Math.toRadians(180), 0);
            case WEST -> new EulerAngle(0, Math.toRadians(-90), 0);
            case EAST -> new EulerAngle(0, Math.toRadians(90), 0);
            default -> new EulerAngle(0, 0, 0);
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
        long removeAfterTicks = Math.clamp(section.getLong("removeAfter", DEF_REMOVE_AFTER_TICKS), 1L, Long.MAX_VALUE);

        return new BulletHoleFeature(enabled, modelData, removeAfterTicks);
    }
}