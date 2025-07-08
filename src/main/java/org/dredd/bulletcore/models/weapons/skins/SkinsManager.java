package org.dredd.bulletcore.models.weapons.skins;

import org.bukkit.entity.Player;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A static utility class for managing weapon skins assigned to players.
 * <p>
 * Maintains a runtime mapping of which players have unlocked which skins
 * for each weapon. This class supports querying, adding, and removing skins
 * per player and weapon.
 * <p>
 * All methods assume they are called on the main server thread.
 *
 * @author dredd
 * @since 1.0.0
 */
public class SkinsManager {

    /**
     * Private constructor to prevent instantiation.
     */
    private SkinsManager() {}

    /**
     * An unmodifiable shared empty list returned by fallback methods.
     */
    private static final List<String> EMPTY = Collections.emptyList();

    /**
     * In-memory storage of skins owned by players.
     * <p>
     * Maps a player's {@link UUID} to a nested map of weapon names and their corresponding
     * unlocked skin names.
     *
     * <pre>{@code
     * UUID playerId -> {
     *     "ak47" -> ["gold", "camo"],
     *     "deagle"  -> ["desert"]
     * }
     * }</pre>
     */
    private static final Map<UUID, Map<String, List<String>>> playerSkinsStorage = new HashMap<>();

    /**
     * Retrieves a specific skin from a given weapon by skin name.
     *
     * @param weapon   the weapon containing available skins
     * @param skinName the name of the skin to retrieve
     * @return the matching {@link WeaponSkin}, or {@code null} if not found
     */
    public static @Nullable WeaponSkin getWeaponSkin(@NotNull Weapon weapon, @NotNull String skinName) {
        return weapon.skins.getSkinOrNull(skinName);
    }

    /**
     * Retrieves a list of weapon names that have at least one skin defined.
     *
     * @return a list of weapon names with skins
     */
    public static @NotNull List<String> getWeaponNamesWithSkins() {
        return CustomItemsRegistry.weapon.getAll().stream()
            .filter(weapon -> weapon.skins.hasSkins())
            .map(weapon -> weapon.name)
            .toList();
    }

    /**
     * Checks if the player has unlocked a specific skin for the given weapon.
     *
     * @param player   the player to check
     * @param weapon   the weapon the skin belongs to
     * @param skinName the skin name to check
     * @return {@code true} if the player has the skin unlocked, {@code false} otherwise
     */
    public static boolean playerHasSkin(@NotNull Player player, @NotNull Weapon weapon, @NotNull String skinName) {
        var playerSkins = playerSkinsStorage.get(player.getUniqueId());
        if (playerSkins == null) return false;
        var playerWeaponSkins = playerSkins.get(weapon.name);
        return playerWeaponSkins != null && playerWeaponSkins.contains(skinName);
    }

    /**
     * Returns an unmodifiable list of skin names the player has unlocked for the given weapon.
     *
     * @param player the player to query
     * @param weapon the weapon to check
     * @return a list of unlocked skin names, or an empty list if none are unlocked
     */
    public static @NotNull List<String> getPlayerWeaponSkins(@NotNull Player player, @NotNull Weapon weapon) {
        var playerSkins = playerSkinsStorage.get(player.getUniqueId());
        if (playerSkins == null) return EMPTY;
        var playerWeaponSkins = playerSkins.get(weapon.name);
        return (playerWeaponSkins == null) ? EMPTY : Collections.unmodifiableList(playerWeaponSkins);
    }

    /**
     * Returns a list of skin names the player does not yet own for a specific weapon.
     *
     * @param player the player to query
     * @param weapon the weapon to check
     * @return a list of missing skin names, or an empty list if all skins are owned or none exist
     */
    public static @NotNull List<String> getMissingWeaponSkins(@NotNull Player player, @NotNull Weapon weapon) {
        if (!weapon.skins.hasSkins()) return EMPTY;
        var playerWeaponSkinNames = getPlayerWeaponSkins(player, weapon);
        return weapon.skins.getSkinNames().stream()
            .filter(skin -> !playerWeaponSkinNames.contains(skin))
            .toList();
    }

    /**
     * Grants a skin to the specified player for the given weapon.
     * <p>
     * The operation fails if the skin does not exist or if the player already owns it.
     *
     * @param player   the player to grant the skin to
     * @param weapon   the weapon the skin belongs to
     * @param skinName the skin name to grant
     * @return {@code true} if the skin was successfully added, {@code false} otherwise
     */
    public static boolean addSkinToPlayer(@NotNull Player player, @NotNull Weapon weapon, @NotNull String skinName) {
        var weaponSkin = getWeaponSkin(weapon, skinName);
        if (weaponSkin == null) return false;
        if (playerHasSkin(player, weapon, skinName)) return false;

        var playerSkins = playerSkinsStorage.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        var playerWeaponSkins = playerSkins.computeIfAbsent(weapon.name, k -> new ArrayList<>());
        return playerWeaponSkins.add(skinName);
    }

    /**
     * Grants all the missing skins to the specified player for the given weapon.
     *
     * @param player the player to grant the skins to.
     * @param weapon the weapon the skins belong to.
     * @return the number of skins successfully added to the player, or 0 if none were added.
     */
    public static int addAllWeaponSkinsToPlayer(@NotNull Player player, @NotNull Weapon weapon) {
        var missingWeaponSkins = getMissingWeaponSkins(player, weapon);
        if (missingWeaponSkins.isEmpty()) return 0;

        var playerSkins = playerSkinsStorage.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        var playerWeaponSkins = playerSkins.computeIfAbsent(weapon.name, k -> new ArrayList<>());
        playerWeaponSkins.addAll(missingWeaponSkins);

        return missingWeaponSkins.size();
    }

    /**
     * Removes a skin from the specified player for the given weapon.
     * <p>
     * If the player does not have the skin, the operation has no effect.
     *
     * @param player   the player to remove the skin from
     * @param weapon   the weapon the skin belongs to
     * @param skinName the skin name to remove
     * @return {@code true} if the skin was successfully removed, {@code false} otherwise
     */
    public static boolean removeSkinFromPlayer(@NotNull Player player, @NotNull Weapon weapon, @NotNull String skinName) {
        var playerSkins = playerSkinsStorage.get(player.getUniqueId());
        if (playerSkins == null) return false;

        var playerWeaponSkins = playerSkins.get(weapon.name);
        if (playerWeaponSkins == null) return false;

        return playerWeaponSkins.remove(skinName);
    }

    /**
     * Removes all the owned skins from the specified player for the given weapon.
     *
     * @param player the player to remove the skins from.
     * @param weapon the weapon the skins belong to.
     * @return the number of skins successfully removed from the player, or 0 if none were removed.
     */
    public static int removeAllWeaponSkinsToPlayer(@NotNull Player player, @NotNull Weapon weapon) {
        var playerSkins = playerSkinsStorage.get(player.getUniqueId());
        if (playerSkins == null) return 0;

        var playerWeaponSkins = playerSkins.get(weapon.name);
        if (playerWeaponSkins == null) return 0;

        int size = playerWeaponSkins.size();
        playerWeaponSkins.clear();

        return size;
    }
}