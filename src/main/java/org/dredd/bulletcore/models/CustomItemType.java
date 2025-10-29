package org.dredd.bulletcore.models;

import org.bukkit.configuration.file.YamlConfiguration;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.custom_item_manager.exceptions.ItemLoadException;
import org.dredd.bulletcore.custom_item_manager.exceptions.ItemRegisterException;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.models.ammo.Ammo;
import org.dredd.bulletcore.models.armor.Armor;
import org.dredd.bulletcore.models.grenades.Grenade;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Locale;

/**
 * Represents all types of {@link CustomBase} items
 * and provides utility methods for loading them.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum CustomItemType {

    // ----------< Enum Fields >----------

    AMMO("ammo", "Ammo", Ammo::new),
    ARMOR("armor", "Armor", Armor::new),
    GRENADE("grenades", "Grenade", Grenade::new),
    WEAPON("weapons", "Weapon", Weapon::new);


    // ----------< Static >----------

    /**
     * Base directory path where all custom item type folders are located.
     */
    private static final String BASE_FOLDER = "custom-items/";

    // -----< Loader >-----

    /**
     * Loads all custom item types from their respective folders and registers them.
     */
    public static void load(@NotNull BulletCore plugin) {
        for (final var type : values())
            type.load0(plugin);
    }


    // ----------< Instance >----------

    /**
     * The path for items of this type inside the plugin's data folder.
     */
    public final String folderPath;

    /**
     * The label for this item type shown in logs during the loading process.
     */
    private final String label;

    /**
     * The "recipe" for constructing custom item instances.
     */
    private final CustomItemLoader loader;

    CustomItemType(@NotNull String subfolder,
                   @NotNull String label,
                   @NotNull CustomItemLoader loader) {
        this.folderPath = BASE_FOLDER + subfolder;
        this.label = label;
        this.loader = loader;
    }

    // -----< Type Loader >-----

    /**
     * Loads all YAML files from their respective folder within the plugin's data folder.
     * <p>
     * For each valid and enabled file:
     * <ul>
     *   <li>Parses the YAML configuration</li>
     *   <li>Constructs the item via its {@link #loader}</li>
     *   <li>Registers it in {@link CustomItemsRegistry}</li>
     * </ul>
     * Invalid or disabled files are skipped. Logs the total number of items loaded.
     *
     * @param plugin the plugin instance used for file access and logging
     */
    private void load0(@NotNull BulletCore plugin) {
        final File folder = new File(plugin.getDataFolder(), folderPath);

        if (!folder.exists() && !folder.mkdirs()) {
            plugin.logError("Could not create folder for " + label + ": " + folder.getPath());
            return;
        }

        final File[] files = folder.listFiles((dir, name) -> {
            String lower = name.toLowerCase(Locale.ROOT);
            return lower.endsWith(".yml") || lower.endsWith(".yaml");
        });

        if (files == null || files.length == 0) {
            plugin.logInfo("No " + label + " definitions found in: " + folder.getPath());
            return;
        }

        int loadedCount = 0;

        for (final File file : files) {
            try {
                final var config = new YamlConfiguration();
                config.load(file);

                if (config.getBoolean("enabled", true)) {
                    CustomBase item = loader.load(config);
                    CustomItemsRegistry.register(item);
                    loadedCount++;
                }

            } catch (ItemLoadException | ItemRegisterException e) {
                plugin.logError("Skipping " + label + " \"" + file.getName() + "\": " + e.getMessage());
            } catch (Exception e) {
                plugin.logError("Failed to load " + label + " file \"" + file.getName() + "\": " + e.getMessage());
            }
        }

        plugin.logInfo("-Loaded " + loadedCount + " " + label + (loadedCount == 1 ? "" : "s"));
    }
}