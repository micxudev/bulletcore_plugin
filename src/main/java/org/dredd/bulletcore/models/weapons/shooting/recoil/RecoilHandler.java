package org.dredd.bulletcore.models.weapons.shooting.recoil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;

/**
 * Handles recoil tasks for players.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class RecoilHandler {

    /**
     * Private constructor to prevent instantiation.
     */
    private RecoilHandler() {}

    /**
     * Stores the recoil data for each player.
     */
    private static final Map<UUID, PlayerRecoil> RECOILS = new HashMap<>();

    /**
     * Stores currently running recoil tasks for each player.
     */
    private static final Map<UUID, BukkitTask> RECOIL_TASKS = new HashMap<>();

    // ----------< Public API >----------

    /**
     * Returns the existing or newly created {@link PlayerRecoil} instance for the given player.<br>
     * Used to track recoil state across the shots.
     *
     * @param player the player to get the recoil for
     * @return {@link PlayerRecoil} instance for the given player
     */
    public static @NotNull PlayerRecoil getRecoil(@NotNull Player player) {
        return RECOILS.computeIfAbsent(player.getUniqueId(), k -> new PlayerRecoil(player));
    }

    /**
     * Stops the recoil task for the given player.
     *
     * @param player the player whose task should be stopped
     */
    public static void cancelRecoilTask(@NotNull Player player) {
        final BukkitTask task = RECOIL_TASKS.remove(player.getUniqueId());
        if (task != null) task.cancel();
    }

    /**
     * Stops the recoil task and clears {@link PlayerRecoil} instance for the given player.
     *
     * @param player the player whose recoil data should be cleared
     */
    public static void cancelAndRemoveRecoil(@NotNull Player player) {
        cancelRecoilTask(player);
        RECOILS.remove(player.getUniqueId());
    }

    /**
     * Stops all the recoil tasks and clears all the recoil data for all the players.
     */
    public static void cancelAllRecoilTasks() {
        RECOIL_TASKS.values().forEach(BukkitTask::cancel);
        RECOIL_TASKS.clear();
        RECOILS.clear();
    }

    /**
     * Handles the recoil logic for a shot fired by the given player.<br>
     * If no recoil task is currently running for the player, a new one is scheduled.<br>
     *
     * @param player       the player who fired the shot; must not be null
     * @param weaponRecoil the recoil config of the used weapon; must not be null
     */
    public static void handleShot(@NotNull Player player,
                                  @NotNull WeaponRecoil weaponRecoil) {
        final var playerRecoil = getRecoil(player);
        playerRecoil.onShotFired(weaponRecoil);

        final UUID playerId = player.getUniqueId();
        if (RECOIL_TASKS.containsKey(playerId)) return;

        final BukkitTask recoilTask = Bukkit.getScheduler().runTaskTimer(
            BulletCore.instance(), playerRecoil::tick, 0L, 1L
        );
        RECOIL_TASKS.put(playerId, recoilTask);
    }
}