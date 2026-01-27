package org.dredd.bulletcore.listeners.trackers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dredd.bulletcore.models.armor.ArmorHit;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.models.weapons.shooting.CurrentHit;
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

    /**
     * Currently processing hit(s).
     * <p>
     * List is used since size should be very small (0-2 entries).
     */
    private static final List<CurrentHit> CURRENT_HITS = new ArrayList<>(4);

    /**
     * Compares the specified {@link CurrentHit} instance with the given attacker and victim UUIDs
     * to check if they represent the same hit.
     *
     * @param hit     the {@code CurrentHit} instance to compare
     * @param damager the UUID of the entity that caused the damage
     * @param victim  the UUID of the entity that received the damage
     * @return {@code true} if the {@code damager} and {@code victim} in the {@code CurrentHit} instance
     * match the provided UUIDs, or {@code false} otherwise
     */
    private static boolean hitEqual(@NotNull CurrentHit hit,
                                    @NotNull UUID damager,
                                    @NotNull UUID victim) {
        return hit.damager().equals(damager) && hit.victim().equals(victim);
    }

    /**
     * Marks that a hit is currently being processed.
     *
     * @param damager UUID of the entity that is attacking
     * @param victim  UUID of the entity that is being attacked
     * @param weapon  the weapon used for this hit
     */
    public static void startHitProcess(@NotNull UUID damager,
                                       @NotNull UUID victim,
                                       @NotNull Weapon weapon) {
        CURRENT_HITS.add(new CurrentHit(damager, victim, weapon));
    }

    /**
     * Marks that a hit has finished being processed.
     *
     * @param damager UUID of the entity that had been attacking
     * @param victim  UUID of the entity that had been attacked
     */
    public static void finishHitProcess(@NotNull UUID damager,
                                        @NotNull UUID victim) {
        CURRENT_HITS.removeIf(hit -> hitEqual(hit, damager, victim));
    }

    /**
     * Gets a hit that is currently being processed.
     *
     * @param damager UUID of the entity that is attacking
     * @param victim  UUID of the entity that is being attacked
     * @return the current hit instance, or {@code null} if not found
     */
    public static @Nullable CurrentHit getCurrentHit(@NotNull UUID damager,
                                                     @NotNull UUID victim) {
        for (final CurrentHit hit : CURRENT_HITS)
            if (hitEqual(hit, damager, victim)) return hit;
        return null;
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
        return getCurrentHit(damager, victim) != null;
    }

    // ----------< CURRENT_ARMOR_HITS >----------

    private static final Map<UUID, ArmorHit> CURRENT_ARMOR_HITS = new HashMap<>();

    /**
     * Adds armor hit.
     *
     * @param victim   the victim's UUID
     * @param armorHit the armor hit data
     */
    public static void addArmorHit(@NotNull UUID victim,
                                   @NotNull ArmorHit armorHit) {
        CURRENT_ARMOR_HITS.put(victim, armorHit);
    }

    /**
     * Removes the armor hit for the given victim.
     *
     * @param victim the victim's UUID
     */
    public static void removeArmorHit(@NotNull UUID victim) {
        CURRENT_ARMOR_HITS.remove(victim);
    }

    /**
     * Gets the armor hit for the given victim.
     *
     * @param victim the victim's UUID
     * @return the armor hit instance, or {@code null} if not found
     */
    public static @Nullable ArmorHit getArmorHit(@NotNull UUID victim) {
        return CURRENT_ARMOR_HITS.get(victim);
    }
}