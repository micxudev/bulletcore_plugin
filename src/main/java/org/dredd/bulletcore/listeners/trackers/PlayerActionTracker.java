package org.dredd.bulletcore.listeners.trackers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks the most recent interactions (e.g., inventory clicks) for each player.
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
     * Clears the recorded inventory interaction time for the given player.
     *
     * @param uuid the unique ID of the player
     */
    public void clear(UUID uuid) {
        lastInventoryInteraction.remove(uuid);
    }
}