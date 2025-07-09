package org.dredd.bulletcore.models.weapons.skins;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages a collection of {@link WeaponSkin} instances associated with a weapon.
 *
 * @author dredd
 * @since 1.0.0
 */
public class WeaponSkins {

    /**
     * A mapping of skin names to their corresponding {@link WeaponSkin} instances.
     */
    private final Map<String, WeaponSkin> skins;

    /**
     * The default weapon skin used to reset/clear the skin on the weapon.
     */
    public final WeaponSkin defaultSkin;

    /**
     * Constructs an empty {@link WeaponSkins} instance with the default skin already set.<br>
     * Use {@link #load(YamlConfiguration, int, Component)} to create an instance.
     */
    private WeaponSkins(int modelData, @NotNull Component displayName) {
        this.skins = new HashMap<>();
        this.defaultSkin = new WeaponSkin(modelData, displayName);
    }

    /**
     * Adds a skin to the collection under the given name.
     *
     * @param name the unique identifier for the skin (e.g., {@code gold}, {@code dragon})
     * @param skin the {@link WeaponSkin} instance to register
     */
    private void addSkin(@NotNull String name, @NotNull WeaponSkin skin) {
        skins.put(name, skin);
    }

    /**
     * Retrieves a registered skin by name, or {@code null} if no such skin exists.
     *
     * @param skinName the name of the skin to retrieve
     * @return the corresponding {@link WeaponSkin}, or {@code null} if not found
     */
    public @Nullable WeaponSkin getSkinOrNull(@NotNull String skinName) {
        return skins.get(skinName);
    }

    /**
     * Checks whether any skins are registered.
     *
     * @return {@code true} if at least one skin exists; {@code false} otherwise
     */
    public boolean hasSkins() {
        return !skins.isEmpty();
    }

    /**
     * Returns an immutable view of all registered skin names.
     *
     * @return a set containing the names of all registered skins
     */
    public @NotNull Set<String> getSkinNames() {
        return Set.copyOf(skins.keySet());
    }


    /**
     * Loads weapon skins from a YAML configuration section.
     * <p>
     * Skins are expected under a {@code "skins"} section.
     * Each skin will be assigned a unique custom model data value
     * incrementally starting from the provided {@code modelData}.<br>
     * If a skin does not specify a {@code "displayName"}, the fallback {@code displayName} is used.
     *
     * @param config      the configuration file to read from
     * @param modelData   the starting custom model data value for skins
     * @param displayName the fallback display name to use if not defined per skin
     * @return a new {@code WeaponSkins} instance populated from configuration
     */
    public static @NotNull WeaponSkins load(@NotNull YamlConfiguration config, int modelData, @NotNull Component displayName) {
        WeaponSkins weaponSkins = new WeaponSkins(modelData, displayName);

        ConfigurationSection skinsSection = config.getConfigurationSection("skins");
        if (skinsSection == null)
            return weaponSkins;

        int skinModelData = modelData;

        for (String key : skinsSection.getKeys(false)) {
            skinModelData++;
            ConfigurationSection skinSection = skinsSection.getConfigurationSection(key);
            if (skinSection == null) {
                Bukkit.getLogger().severe("Invalid skin definition for skin name '" + key + "'");
                continue;
            }

            Component skinDisplayName = skinSection.getRichMessage("displayName", displayName);

            WeaponSkin skin = new WeaponSkin(skinModelData, skinDisplayName);
            weaponSkins.addSkin(key, skin);
        }

        return weaponSkins;
    }
}