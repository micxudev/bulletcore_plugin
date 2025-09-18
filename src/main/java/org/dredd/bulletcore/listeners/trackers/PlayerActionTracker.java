package org.dredd.bulletcore.listeners.trackers;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks recent player actions such as inventory interactions, item drops,
 * and single shots with automatic weapons.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class PlayerActionTracker {

    private PlayerActionTracker() {}

    private static final Map<UUID, Long> LAST_INVENTORY_INTERACTION = new HashMap<>();

    private static final Map<UUID, Long> LAST_DROP = new HashMap<>();

    private static final Map<UUID, Long> LAST_SINGLE_SHOT_USING_AUTOMATIC_WEAPON = new HashMap<>();

    /**
     * Records the current time as the player's last inventory interaction.
     */
    public static void recordInventoryInteraction(@NotNull UUID uuid) {
        LAST_INVENTORY_INTERACTION.put(uuid, System.currentTimeMillis());
    }

    /**
     * Gets the last recorded inventory interaction time for the player.
     *
     * @return timestamp in ms, or {@code 0} if none recorded
     */
    public static long getLastInventoryInteraction(@NotNull UUID uuid) {
        return LAST_INVENTORY_INTERACTION.getOrDefault(uuid, 0L);
    }

    /**
     * Records the current time as the player's last item drop.
     */
    public static void recordDrop(@NotNull UUID uuid) {
        LAST_DROP.put(uuid, System.currentTimeMillis());
    }

    /**
     * Gets the last recorded item drop time for the player.
     *
     * @return timestamp in ms, or {@code 0} if none recorded
     */
    public static long getLastDrop(@NotNull UUID uuid) {
        return LAST_DROP.getOrDefault(uuid, 0L);
    }

    /**
     * Records the current time as the player's last single shot with an automatic weapon.
     */
    public static void recordSingleShotAutomatic(@NotNull UUID uuid) {
        LAST_SINGLE_SHOT_USING_AUTOMATIC_WEAPON.put(uuid, System.currentTimeMillis());
    }

    /**
     * Gets the last recorded single shot time with an automatic weapon.
     *
     * @return timestamp in ms, or {@code 0} if none recorded
     */
    public static long getLastSingleShotAutomatic(@NotNull UUID uuid) {
        return LAST_SINGLE_SHOT_USING_AUTOMATIC_WEAPON.getOrDefault(uuid, 0L);
    }

    /**
     * Removes all tracked data for the given player.
     */
    public static void clear(@NotNull UUID uuid) {
        LAST_INVENTORY_INTERACTION.remove(uuid);
        LAST_DROP.remove(uuid);
        LAST_SINGLE_SHOT_USING_AUTOMATIC_WEAPON.remove(uuid);
    }
}