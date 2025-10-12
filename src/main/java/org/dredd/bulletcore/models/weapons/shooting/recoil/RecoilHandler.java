package org.dredd.bulletcore.models.weapons.shooting.recoil;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles recoil tasks for players.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class RecoilHandler {

    /**
     * Stores the recoil data for each player.
     */
    private static final Map<UUID, PlayerRecoil> recoils = new HashMap<>();

    /**
     * Stores currently running recoil tasks for each player.
     */
    private static final Map<UUID, BukkitTask> recoilTasks = new HashMap<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private RecoilHandler() {}

    /**
     * Returns the existing or newly created {@link PlayerRecoil} instance for the given player.<br>
     * Used to track recoil state across shots.
     *
     * @param player the player to get the recoil for
     * @return {@link PlayerRecoil} instance for the given player
     */
    public static @NotNull PlayerRecoil getRecoil(@NotNull Player player) {
        return recoils.computeIfAbsent(player.getUniqueId(), k -> new PlayerRecoil(player));
    }

    /**
     * Handles the recoil logic for a shot fired by the given player.<br>
     * If no recoil task is currently running for the player, a new one is scheduled.<br>
     *
     * @param player       the player who fired the shot; must not be null
     * @param weaponRecoil the recoil config of the used weapon; must not be null
     */
    public static void handleShot(@NotNull Player player, @NotNull WeaponRecoil weaponRecoil) {
        PlayerRecoil playerRecoil = getRecoil(player);
        playerRecoil.onShotFired(weaponRecoil);

        UUID playerId = player.getUniqueId();
        if (recoilTasks.containsKey(playerId)) return;

        BukkitTask recoilTask = Bukkit.getScheduler().runTaskTimer(BulletCore.instance(),
            playerRecoil::tick, 0L, 1L
        );
        recoilTasks.put(playerId, recoilTask);
    }

    /**
     * Stops the recoil task for the given player. If the player has no task, nothing is changed.
     *
     * @param player the player whose task to stop
     */
    public static void stopRecoilTask(@NotNull Player player) {
        BukkitTask task = recoilTasks.remove(player.getUniqueId());
        if (task != null) task.cancel();
    }

    /**
     * Stops the recoil task and clears {@link PlayerRecoil} instance for the given player.
     *
     * @param player the player whose recoil data should be cleared
     */
    public static void stopAndClearRecoil(@NotNull Player player) {
        stopRecoilTask(player);
        recoils.remove(player.getUniqueId());
    }

    /**
     * Stops all the recoil tasks and clears all the recoil data for all the players.
     */
    public static void cancelAllRecoilTasks() {
        recoilTasks.values().forEach(BukkitTask::cancel);
        recoilTasks.clear();
        recoils.clear();
    }
}