package org.dredd.bulletcore.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.custom_item_manager.exceptions.ItemLoadException;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.models.CustomBase;
import org.dredd.bulletcore.models.CustomItemType;
import org.dredd.bulletcore.models.ammo.Ammo;
import org.dredd.bulletcore.models.armor.Armor;
import org.dredd.bulletcore.models.grenades.Grenade;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.utils.ThrowingFunction;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class for loading all custom item models from YML files.
 * <p>
 * This loader reads configuration files for all supported {@link CustomItemType}s
 * and deserializes them into their respective object types, registering them into their proper registries.
 * <p>
 * The loader supports a shared base attribute parser and individual model loaders for each item type.
 * <p>
 * Invalid or disabled files will be skipped with appropriate logging.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class YMLItemLoader {

    /**
     * Private constructor to prevent instantiation.
     */
    private YMLItemLoader() {}

    /**
     * Functional interface for loading a custom item from a {@link YamlConfiguration}.
     *
     * @param <T> the type of {@link CustomBase} item to load
     */
    @FunctionalInterface
    private interface ItemLoader<T extends CustomBase> {

        /**
         * Loads an item from the given configuration.
         *
         * @param config the YAML configuration
         * @return the loaded item
         * @throws ItemLoadException if the config is invalid or incomplete
         */
        @NotNull T load(@NotNull YamlConfiguration config) throws ItemLoadException;
    }

    /**
     * Loads all custom items for each supported item type from their respective folders
     * and registers them into their specific and global registries.
     */
    public static void loadAllItems() {
        Map<CustomItemType, ItemLoader<?>> loaders = new LinkedHashMap<>() {{
            put(CustomItemType.AMMO, Ammo::new);
            put(CustomItemType.ARMOR, Armor::new);
            put(CustomItemType.GRENADE, Grenade::new);
            put(CustomItemType.WEAPON, Weapon::new);
        }};

        loaders.forEach((type, loader) ->
            loadFolder(type, config -> {
                CustomBase item = loader.load(config);
                CustomItemsRegistry.register(item);
                return true;
            })
        );
    }

    /**
     * Loads all YAML files in the given item type's folder and applies the provided config processor.
     *
     * <p>Skips files that are disabled or fail validation and logs the total number of successfully
     * loaded items.</p>
     *
     * @param type            the custom item type
     * @param configProcessor a function to process each valid YAML config
     */
    private static void loadFolder(@NotNull CustomItemType type,
                                   @NotNull ThrowingFunction<YamlConfiguration, Boolean> configProcessor) {
        File folder = new File(BulletCore.instance().getDataFolder(), type.getFolderPath());
        if (!folder.exists() && !folder.mkdirs()) {
            BulletCore.logError("Failed to create directory: " + folder.getPath());
            return;
        }

        File[] files = folder.listFiles(
            (dir, name) -> {
                String lower = name.toLowerCase(Locale.ROOT);
                return lower.endsWith(".yml") || lower.endsWith(".yaml");
            });
        if (files == null || files.length == 0) {
            BulletCore.logInfo("No " + type.getLabel() + " files found in " + folder.getPath());
            return;
        }

        int loadedCount = 0;
        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                if (!config.getBoolean("enabled", true)) continue;
                if (configProcessor.apply(config)) loadedCount++;
            } catch (ItemLoadException e) {
                BulletCore.logError(e.getMessage() + "; In file: " + file.getName() + "; Skipping the item.");
            } catch (Exception e) {
                BulletCore.logError("Error loading " + file.getName() + ": " + e.getMessage());
            }
        }

        BulletCore.logInfo("-Loaded " + loadedCount + " " + type.getLabel() + " types");
    }
}