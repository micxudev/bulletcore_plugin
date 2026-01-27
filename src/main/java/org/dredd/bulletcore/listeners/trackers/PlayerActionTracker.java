package org.dredd.bulletcore.listeners.trackers;

import java.util.UUID;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.jetbrains.annotations.NotNull;

/**
 * Tracks the most recent player actions.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class PlayerActionTracker {

    /**
     * Private constructor to prevent instantiation.
     */
    private PlayerActionTracker() {}

    // ----------< LAST_INVENTORY_INTERACTION >----------

    private static final Object2LongMap<UUID> LAST_INVENTORY_INTERACTION = new Object2LongOpenHashMap<>();

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

    // ----------< LAST_DROP >----------

    private static final Object2LongMap<UUID> LAST_DROP = new Object2LongOpenHashMap<>();

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

    // ----------< LAST_SINGLE_SHOT_USING_AUTOMATIC_WEAPON >----------

    private static final Object2LongMap<UUID> LAST_SINGLE_SHOT_USING_AUTOMATIC_WEAPON = new Object2LongOpenHashMap<>();

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

    // ----------< SHARED >----------

    /**
     * Removes all tracked data for the given player UUID.
     */
    public static void clear(@NotNull UUID uuid) {
        LAST_INVENTORY_INTERACTION.removeLong(uuid);
        LAST_DROP.removeLong(uuid);
        LAST_SINGLE_SHOT_USING_AUTOMATIC_WEAPON.removeLong(uuid);
    }
}