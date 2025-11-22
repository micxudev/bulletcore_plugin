package org.dredd.bulletcore.armorstand_features;

import io.papermc.paper.math.Rotations;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for spawning and managing invisible armor stands used for visuals.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class ArmorStandHandler {

    /**
     * Private constructor to prevent instantiation.
     */
    private ArmorStandHandler() {}

    // ----------< Public API >----------

    /**
     * Spawns an invisible, static armor stand with the given head item and rotation.
     *
     * @param world         the world to spawn in
     * @param spawnLocation the location to place the stand
     * @param headItem      the item to display on the head
     * @param headRotations the head rotation
     * @return the spawned armor stand
     */
    public static @NotNull ArmorStand spawn(@NotNull World world,
                                            @NotNull Location spawnLocation,
                                            @NotNull ItemStack headItem,
                                            @NotNull Rotations headRotations) {
        return world.spawn(
            spawnLocation, ArmorStand.class, a -> {
                a.setInvisible(true);
                a.setSmall(true);
                a.setArms(false);
                a.setBasePlate(false);
                a.setMarker(true);
                a.setGravity(false);
                a.setPersistent(false);
                a.setInvulnerable(true);
                a.setSilent(true);
                a.setCanMove(false);
                a.setCanTick(false);
                a.getEquipment().setHelmet(headItem, true);
                a.setHeadRotations(headRotations);
            }
        );
    }

    /**
     * Removes the given armor stand after the specified delay in ticks.
     *
     * @param stand            the armor stand to remove
     * @param removeAfterTicks number of ticks before removing (20 ticks = 1 second)
     */
    public static void scheduleRemoval(@NotNull ArmorStand stand,
                                       long removeAfterTicks) {
        Bukkit.getScheduler().runTaskLater(BulletCore.instance(), stand::remove, removeAfterTicks);
    }
}