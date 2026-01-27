package org.dredd.bulletcore.models.weapons.skins;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.models.CustomItemType;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.utils.JsonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tools.jackson.core.type.TypeReference;

import static org.dredd.bulletcore.utils.ServerUtils.EMPTY_LIST;

/**
 * Utility class for managing player weapon skins.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class SkinsManager {

    /**
     * Private constructor to prevent instantiation.
     */
    private SkinsManager() {}

    /**
     * Represents the file where skins data is stored and managed.
     */
    private static File skinsDataFile;

    /**
     * Skins storage format:
     * <pre>{@code
     * PlayerUUID -> {
     *     "ak47" -> ["gold", "camo"],
     *     "deagle"  -> ["desert"]
     * }
     * }</pre>
     */
    private static Map<UUID, Map<String, List<String>>> playerSkinsStorage;

    // ----------< Init >----------

    /**
     * Loads the skin data from the file and initializes the skins' storage.
     */
    public static void load(@NotNull BulletCore plugin) {
        skinsDataFile = new File(
            plugin.getDataFolder(),
            CustomItemType.WEAPON.folderPath + "/data/skins.json"
        );
        playerSkinsStorage = JsonUtils.load(skinsDataFile, new TypeReference<>() {}, new HashMap<>());
    }

    /**
     * Asynchronously saves the skin data to the file.
     */
    private static void save() {
        JsonUtils.saveAsync(playerSkinsStorage, skinsDataFile, true);
    }

    // ----------< Public API >----------

    /**
     * Retrieves a specific skin from a given weapon by skin name.
     *
     * @param weapon   the weapon containing available skins
     * @param skinName the name of the skin to retrieve
     * @return the matching {@link WeaponSkin}, or {@code null} if not found
     */
    public static @Nullable WeaponSkin getWeaponSkin(@NotNull Weapon weapon,
                                                     @NotNull String skinName) {
        return weapon.skins.getSkinOrNull(skinName);
    }

    /**
     * Retrieves a list of weapon names that have at least one skin defined.
     *
     * @return a list of weapon names with skins
     */
    public static @NotNull List<String> getWeaponNamesWithSkins() {
        return CustomItemsRegistry.WEAPON.getAll().stream()
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
    public static boolean playerHasSkin(@NotNull Player player,
                                        @NotNull Weapon weapon,
                                        @NotNull String skinName) {
        final var playerSkins = playerSkinsStorage.get(player.getUniqueId());
        if (playerSkins == null) return false;
        final var weaponSkins = playerSkins.get(weapon.name);
        return weaponSkins != null && weaponSkins.contains(skinName);
    }

    /**
     * Returns an unmodifiable list of skin names the player has unlocked for the given weapon.
     *
     * @param player the player to query
     * @param weapon the weapon to check
     * @return a list of unlocked skin names, or an empty list if none are unlocked
     */
    public static @NotNull List<String> getPlayerWeaponSkins(@NotNull Player player,
                                                             @NotNull Weapon weapon) {
        final var playerSkins = playerSkinsStorage.get(player.getUniqueId());
        if (playerSkins == null) return EMPTY_LIST;
        final var weaponSkins = playerSkins.get(weapon.name);
        return (weaponSkins == null) ? EMPTY_LIST : Collections.unmodifiableList(weaponSkins);
    }

    /**
     * Returns a list of skin names the player does not yet own for a specific weapon.
     *
     * @param player the player to query
     * @param weapon the weapon to check
     * @return a list of missing skin names, or an empty list if all skins are owned or none exist
     */
    public static @NotNull List<String> getMissingWeaponSkins(@NotNull Player player,
                                                              @NotNull Weapon weapon) {
        if (!weapon.skins.hasSkins()) return EMPTY_LIST;
        final var weaponSkinNames = getPlayerWeaponSkins(player, weapon);
        return weapon.skins.getSkinNames().stream()
            .filter(skin -> !weaponSkinNames.contains(skin))
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
    public static boolean addSkinToPlayer(@NotNull Player player,
                                          @NotNull Weapon weapon,
                                          @NotNull String skinName) {
        final var weaponSkin = getWeaponSkin(weapon, skinName);
        if (weaponSkin == null) return false;
        if (playerHasSkin(player, weapon, skinName)) return false;

        final var playerSkins = playerSkinsStorage.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        final var weaponSkins = playerSkins.computeIfAbsent(weapon.name, k -> new ArrayList<>());
        weaponSkins.add(skinName);
        save();
        return true;
    }

    /**
     * Grants all the missing skins to the specified player for the given weapon.
     *
     * @param player the player to grant the skins to.
     * @param weapon the weapon the skins belong to.
     * @return the number of skins successfully added to the player, or 0 if none were added.
     */
    public static int addAllWeaponSkinsToPlayer(@NotNull Player player,
                                                @NotNull Weapon weapon) {
        final var missingWeaponSkins = getMissingWeaponSkins(player, weapon);
        if (missingWeaponSkins.isEmpty()) return 0;

        final var playerSkins = playerSkinsStorage.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        final var weaponSkins = playerSkins.computeIfAbsent(weapon.name, k -> new ArrayList<>());
        weaponSkins.addAll(missingWeaponSkins);
        save();

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
    public static boolean removeSkinFromPlayer(@NotNull Player player,
                                               @NotNull Weapon weapon,
                                               @NotNull String skinName) {
        final var playerSkins = playerSkinsStorage.get(player.getUniqueId());
        if (playerSkins == null) return false;

        final var weaponSkins = playerSkins.get(weapon.name);
        if (weaponSkins == null) return false;

        final boolean remove = weaponSkins.remove(skinName);
        if (!remove) return false;

        // Clean up empty lists and maps
        if (weaponSkins.isEmpty()) {
            playerSkins.remove(weapon.name);
            if (playerSkins.isEmpty())
                playerSkinsStorage.remove(player.getUniqueId());
        }

        save();
        return true;
    }

    /**
     * Removes all the owned skins from the specified player for the given weapon.
     *
     * @param player the player to remove the skins from.
     * @param weapon the weapon the skins belong to.
     * @return the number of skins successfully removed from the player, or 0 if none were removed.
     */
    public static int removeAllWeaponSkinsFromPlayer(@NotNull Player player,
                                                     @NotNull Weapon weapon) {
        final var playerSkins = playerSkinsStorage.get(player.getUniqueId());
        if (playerSkins == null) return 0;

        final var weaponSkins = playerSkins.remove(weapon.name);
        if (weaponSkins == null) return 0;
        if (playerSkins.isEmpty())
            playerSkinsStorage.remove(player.getUniqueId());

        save();
        return weaponSkins.size();
    }
}