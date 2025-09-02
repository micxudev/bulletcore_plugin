package org.dredd.bulletcore.listeners.trackers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks the most recent interactions (e.g., inventory clicks, item drops) for each player.
 *
 * @author dredd
 * @since 1.0.0
 */
public class PlayerActionTracker {

    /**
     * Stores last inventory interaction time for each player.
     */
    private final Map<UUID, Long> lastInventoryInteraction = new HashMap<>();

    /**
     * Stores last item drop time for each player.
     */
    private final Map<UUID, Long> lastDrop = new HashMap<>();

    /**
     * Stores last time each player fired a single shot using an automatic weapon.
     */
    private static final Map<UUID, Long> lastSingleShotUsingAutomaticWeapon = new HashMap<>();

    /**
     * Marks the current time as the player's last inventory interaction.
     *
     * @param uuid the unique ID of the player
     */
    public void markInventoryInteraction(UUID uuid) {
        lastInventoryInteraction.put(uuid, System.currentTimeMillis());
    }

    /**
     * Returns the timestamp (in milliseconds) of the player's last inventory interaction.
     * If no interaction has been recorded, returns {@code 0}.
     *
     * @param uuid the unique ID of the player
     * @return the last interaction time in milliseconds, or {@code 0} if none recorded
     */
    public long getLastInventoryInteraction(UUID uuid) {
        return lastInventoryInteraction.getOrDefault(uuid, 0L);
    }

    /**
     * Marks the current time as the player's last item drop.
     *
     * @param uuid the unique ID of the player
     */
    public void markDrop(UUID uuid) {
        lastDrop.put(uuid, System.currentTimeMillis());
    }

    /**
     * Returns the timestamp (in milliseconds) of the player's last item drop.
     * If no drop has been recorded, returns {@code 0}.
     *
     * @param uuid the unique ID of the player
     * @return the last drop time in milliseconds, or {@code 0} if none recorded
     */
    public long getLastDrop(UUID uuid) {
        return lastDrop.getOrDefault(uuid, 0L);
    }

    /**
     * Updates the last single-shot timestamp for the given player using an automatic weapon.
     */
    public static void updateLastSingleShotUsingAutomaticWeapon(UUID uuid) {
        PlayerActionTracker.lastSingleShotUsingAutomaticWeapon.put(uuid, System.currentTimeMillis());
    }

    /**
     * Gets the timestamp of the last single shot fired with an automatic
     * weapon by the given player. Returns 0 if none recorded.
     */
    public static long getLastSingleShotUsingAutomaticWeapon(UUID uuid) {
        return PlayerActionTracker.lastSingleShotUsingAutomaticWeapon.getOrDefault(uuid, 0L);
    }

    /**
     * Clears the recorded data for the given player.
     *
     * @param uuid the unique ID of the player
     */
    public void clear(UUID uuid) {
        lastInventoryInteraction.remove(uuid);
        lastDrop.remove(uuid);
        PlayerActionTracker.lastSingleShotUsingAutomaticWeapon.remove(uuid);
    }
}