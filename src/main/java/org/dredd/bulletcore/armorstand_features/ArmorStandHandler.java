package org.dredd.bulletcore.armorstand_features;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for spawning and managing invisible armor stands used in visual features.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class ArmorStandHandler {

    /**
     * Private constructor to prevent instantiation.
     */
    private ArmorStandHandler() {}

    /**
     * Spawns an invisible, non-interactive armor stand with the given item and head pose.
     *
     * @param world         the world to spawn in
     * @param spawnLocation the exact location to place the armor stand
     * @param headItem      the item to place on the armor stand's head (model-based visual)
     * @param headPose      the rotation of the armor stand's head
     * @return the spawned armor stand instance
     */
    public static @NotNull ArmorStand spawn(@NotNull World world, @NotNull Location spawnLocation,
                                            @NotNull ItemStack headItem, @NotNull EulerAngle headPose) {
        return world.spawn(spawnLocation, ArmorStand.class, a -> {
            a.setInvisible(true);
            a.setSmall(true);
            a.setArms(false);
            a.setBasePlate(false);
            a.setMarker(true);
            a.setGravity(false);
            a.setPersistent(false);
            a.setInvulnerable(true);
            a.setCanMove(false);
            a.setCanTick(false);
            a.getEquipment().setHelmet(headItem, true);
            a.setHeadPose(headPose);
        });
    }

    /**
     * Schedules automatic removal of an armor stand after a delay.
     *
     * @param stand            the armor stand to remove
     * @param removeAfterTicks how many ticks to wait before removing (20 ticks = 1 second)
     */
    public static void scheduleRemoval(@NotNull ArmorStand stand, long removeAfterTicks) {
        Bukkit.getScheduler().runTaskLater(BulletCore.getInstance(), stand::remove, removeAfterTicks);
    }
}