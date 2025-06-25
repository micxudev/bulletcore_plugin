package org.dredd.bulletcore.listeners.trackers;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks currently processing hit(s) caused by Weapons.
 *
 * @author dredd
 * @since 1.0.0
 */
public class CurrentHitTracker {

    /**
     * Stores currently processing hits.
     */
    private static final Map<UUID, UUID> currentHits = new HashMap<>();

    /**
     * Marks that a hit is currently being processed.
     *
     * @param damager UUID of the entity that is attacking
     * @param victim  UUID of the entity that is being attacked
     */
    public static void startHitProcess(@NotNull UUID damager, @NotNull UUID victim) {
        currentHits.put(damager, victim);
    }

    /**
     * Marks that a hit has finished being processed.
     *
     * @param damager UUID of the entity that had been attacking
     * @param victim  UUID of the entity that had been attacked
     */
    public static void finishHitProcess(@NotNull UUID damager, @NotNull UUID victim) {
        currentHits.remove(damager, victim);
    }

    /**
     * Returns whether a hit is currently being processed.
     *
     * @param damager UUID of the entity that is attacking
     * @param victim  UUID of the entity that is being attacked
     * @return {@code true} if a hit is currently being processed, {@code false} otherwise
     */
    public static boolean isAlreadyHit(@NotNull UUID damager, @NotNull UUID victim) {
        UUID uuid = currentHits.get(damager);
        return uuid != null && uuid.equals(victim);
    }
}