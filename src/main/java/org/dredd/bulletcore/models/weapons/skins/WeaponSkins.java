package org.dredd.bulletcore.models.weapons.skins;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manages a collection of {@link WeaponSkin} instances associated with a weapon.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class WeaponSkins {

    // ----------< Static Loader >----------

    /**
     * Loads weapon skins from a YAML configuration.
     * <p>
     * Skins are expected under a {@code skins} section.<br>
     * Each skin will be assigned a unique custom model data value
     * incrementally starting from the provided {@code modelData}.<br>
     * If a skin does not specify a {@code displayName}, the fallback will be used.
     *
     * @param config      the configuration file to read from
     * @param modelData   the starting custom model data value for skins
     * @param displayName the fallback display name to use if not defined per skin
     * @return a new {@link WeaponSkins} instance with loaded skins
     */
    public static @NotNull WeaponSkins load(@NotNull YamlConfiguration config,
                                            int modelData,
                                            @NotNull Component displayName) {
        final WeaponSkins weaponSkins = new WeaponSkins(modelData, displayName);

        final ConfigurationSection skinsSection = config.getConfigurationSection("skins");
        if (skinsSection == null)
            return weaponSkins;

        int skinModelData = modelData;

        for (final String key : skinsSection.getKeys(false)) {
            skinModelData++;

            if (!CustomItemsRegistry.isValidName(key)) {
                BulletCore.logError("Skipping weapon skin \"" + key + "\": Does not match pattern " + CustomItemsRegistry.VALID_NAME.pattern());
                continue;
            }

            final ConfigurationSection skinSection = skinsSection.getConfigurationSection(key);
            if (skinSection == null) {
                BulletCore.logError("Skipping weapon skin \"" + key + "\": Is not a section");
                continue;
            }

            final Component skinDisplayName = skinSection.getRichMessage("displayName", displayName);

            weaponSkins.addSkin(new WeaponSkin(key, skinModelData, skinDisplayName));
        }

        return weaponSkins;
    }


    // ----------< Instance >----------

    // -----< Attributes >-----

    /**
     * A mapping of skin modelData to their corresponding {@link WeaponSkin} instances.
     */
    private final Int2ObjectMap<WeaponSkin> skinsByModelData;

    /**
     * A mapping of skin names to their corresponding {@link WeaponSkin} instances.
     */
    private final Map<String, WeaponSkin> skinsByName;

    /**
     * The default weapon skin used to reset/clear the skin on the weapon.
     */
    public final WeaponSkin defaultSkin;

    // -----< Construction >-----

    /**
     * Constructs an empty {@link WeaponSkins} instance with the default skin already set.<br>
     * Use {@link #load(YamlConfiguration, int, Component)} to create an instance.
     */
    private WeaponSkins(int modelData,
                        @NotNull Component displayName) {
        this.skinsByModelData = new Int2ObjectOpenHashMap<>();
        this.skinsByName = new HashMap<>();
        this.defaultSkin = new WeaponSkin("--default", modelData, displayName);
    }

    // -----< Internal Registration >-----

    /**
     * Adds a skin to the collection of skins.
     *
     * @param skin the {@link WeaponSkin} instance to register
     */
    private void addSkin(@NotNull WeaponSkin skin) {
        skinsByModelData.put(skin.customModelData(), skin);
        skinsByName.put(skin.name(), skin);
    }

    // -----< Public API >-----

    /**
     * Retrieves a registered skin by custom model data, or {@code null} if no such skin exists.
     *
     * @param customModelData the custom model data of the skin to retrieve
     * @return the corresponding {@link WeaponSkin}, or {@code null} if not found
     */
    public @Nullable WeaponSkin getSkinOrNull(int customModelData) {
        return skinsByModelData.get(customModelData);
    }

    /**
     * Retrieves a registered skin by name, or {@code null} if no such skin exists.
     *
     * @param skinName the name of the skin to retrieve
     * @return the corresponding {@link WeaponSkin}, or {@code null} if not found
     */
    public @Nullable WeaponSkin getSkinOrNull(@NotNull String skinName) {
        return skinsByName.get(skinName);
    }

    /**
     * Checks whether any skins are registered.
     *
     * @return {@code true} if at least one skin exists; {@code false} otherwise
     */
    public boolean hasSkins() {
        return !skinsByName.isEmpty();
    }

    /**
     * Returns an immutable view of all registered skin names.
     *
     * @return a set containing the names of all registered skins
     */
    public @NotNull Set<String> getSkinNames() {
        return Set.copyOf(skinsByName.keySet());
    }

    /**
     * Retrieves the next weapon skin available for the player or default if no other skins are available.
     *
     * @param customModelData   the custom model data of the current weapon skin
     * @param playerWeaponSkins a list of available weapon skin names for the player
     * @return the next {@link WeaponSkin} available for the player, or the default skin
     */
    public @NotNull WeaponSkin getNextOrDefault(int customModelData,
                                                @NotNull List<String> playerWeaponSkins) {
        final var iterator = playerWeaponSkins.listIterator();

        final WeaponSkin currentSkin = getSkinOrNull(customModelData);
        if (currentSkin == null) {
            // customModelData is either default (parent skin) or skin does not exist.
            // Find the first existing skin or default.
            return findNextSkinOrDefault(iterator);
        }

        while (iterator.hasNext())
            if (currentSkin.name().equals(iterator.next()))
                return findNextSkinOrDefault(iterator);

        return defaultSkin;
    }

    // -----< Internal Utilities >-----

    /**
     * Attempts to find the next valid {@link WeaponSkin} from the given iterator.
     *
     * @param iterator an {@link Iterator} of skin names to search through
     * @return the next valid {@link WeaponSkin} if found, or the default skin
     */
    private @NotNull WeaponSkin findNextSkinOrDefault(@NotNull Iterator<String> iterator) {
        while (iterator.hasNext()) {
            final WeaponSkin skin = getSkinOrNull(iterator.next());
            if (skin != null) return skin;
        }
        return defaultSkin;
    }
}