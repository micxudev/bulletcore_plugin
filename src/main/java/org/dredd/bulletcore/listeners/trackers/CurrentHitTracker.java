package org.dredd.bulletcore.listeners.trackers;

import org.dredd.bulletcore.models.armor.ArmorHit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Stores currently processing armor hits.
     */
    private static final Map<UUID, ArmorHit> currentArmorHits = new HashMap<>();


    // -----< currentHits >-----

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


    // -----< currentArmorHits >-----

    /**
     * Adds armor hit.
     *
     * @param victimUUID the victim's UUID
     * @param armorHit   the armor hit data
     */
    public static void addArmorHit(@NotNull UUID victimUUID, @NotNull ArmorHit armorHit) {
        currentArmorHits.put(victimUUID, armorHit);
    }

    /**
     * Removes the armor hit for the given victim.
     *
     * @param victimUUID the victim's UUID
     */
    public static void removeArmorHit(@NotNull UUID victimUUID) {
        currentArmorHits.remove(victimUUID);
    }

    /**
     * Gets the armor hit for the given victim.
     *
     * @param victimUUID the victim's UUID
     * @return the armor hit instance, or {@code null} if not found
     */
    public static @Nullable ArmorHit getArmorHit(@NotNull UUID victimUUID) {
        return currentArmorHits.get(victimUUID);
    }
}