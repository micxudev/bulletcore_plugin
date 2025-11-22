package org.dredd.bulletcore.listeners.trackers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.dredd.bulletcore.models.armor.ArmorHit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Tracks currently processing hit(s) caused by Weapons.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class CurrentHitTracker {

    /**
     * Private constructor to prevent instantiation.
     */
    private CurrentHitTracker() {}

    // ----------< CURRENT_HITS >----------

    private static final Map<UUID, UUID> CURRENT_HITS = new HashMap<>();

    /**
     * Marks that a hit is currently being processed.
     *
     * @param damager UUID of the entity that is attacking
     * @param victim  UUID of the entity that is being attacked
     */
    public static void startHitProcess(@NotNull UUID damager,
                                       @NotNull UUID victim) {
        CURRENT_HITS.put(damager, victim);
    }

    /**
     * Marks that a hit has finished being processed.
     *
     * @param damager UUID of the entity that had been attacking
     * @param victim  UUID of the entity that had been attacked
     */
    public static void finishHitProcess(@NotNull UUID damager,
                                        @NotNull UUID victim) {
        CURRENT_HITS.remove(damager, victim);
    }

    /**
     * Returns whether a hit is currently being processed.
     *
     * @param damager UUID of the entity that is attacking
     * @param victim  UUID of the entity that is being attacked
     * @return {@code true} if a hit is currently being processed, {@code false} otherwise
     */
    public static boolean isAlreadyHit(@NotNull UUID damager,
                                       @NotNull UUID victim) {
        final UUID uuid = CURRENT_HITS.get(damager);
        return uuid != null && uuid.equals(victim);
    }

    // ----------< CURRENT_ARMOR_HITS >----------

    private static final Map<UUID, ArmorHit> CURRENT_ARMOR_HITS = new HashMap<>();

    /**
     * Adds armor hit.
     *
     * @param victimUUID the victim's UUID
     * @param armorHit   the armor hit data
     */
    public static void addArmorHit(@NotNull UUID victimUUID,
                                   @NotNull ArmorHit armorHit) {
        CURRENT_ARMOR_HITS.put(victimUUID, armorHit);
    }

    /**
     * Removes the armor hit for the given victim.
     *
     * @param victimUUID the victim's UUID
     */
    public static void removeArmorHit(@NotNull UUID victimUUID) {
        CURRENT_ARMOR_HITS.remove(victimUUID);
    }

    /**
     * Gets the armor hit for the given victim.
     *
     * @param victimUUID the victim's UUID
     * @return the armor hit instance, or {@code null} if not found
     */
    public static @Nullable ArmorHit getArmorHit(@NotNull UUID victimUUID) {
        return CURRENT_ARMOR_HITS.get(victimUUID);
    }
}