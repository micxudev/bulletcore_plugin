package org.dredd.bulletcore.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.custom_item_manager.MaterialStorage;
import org.dredd.bulletcore.custom_item_manager.exceptions.ItemLoadException;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.models.CustomBase;
import org.dredd.bulletcore.models.CustomBase.BaseAttributes;
import org.dredd.bulletcore.models.CustomItemType;
import org.dredd.bulletcore.models.ammo.Ammo;
import org.dredd.bulletcore.models.armor.Armor;
import org.dredd.bulletcore.models.grenades.Grenade;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.utils.ComponentUtils;
import org.dredd.bulletcore.utils.MathUtils;
import org.dredd.bulletcore.utils.ThrowingFunction;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.dredd.bulletcore.utils.ComponentUtils.WHITE;

/**
 * Utility class for loading all custom item models from YML files.
 *
 * <p>This loader reads configuration files for all supported {@link CustomItemType}s
 * and deserializes them into their respective object types, registering them into their proper registries.</p>
 *
 * <p>The loader supports a shared base attribute parser and individual model loaders for each item type.</p>
 *
 * <p>Invalid or disabled files will be skipped with appropriate logging.</p>
 *
 * @author dredd
 * @since 1.0.0
 */
public final class YMLLModelLoader {

    /**
     * File extension used for model configuration files.
     */
    private static final String YML_EXTENSION = ".yml";

    /**
     * Reference to the plugin's main instance, used for data folder access and logging.
     */
    private static final BulletCore plugin = BulletCore.getInstance();

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
        T load(YamlConfiguration config) throws ItemLoadException;
    }

    /**
     * Loads all custom items for each supported item type from their respective folders
     * and registers them into their specific and global registries.
     */
    public static void loadAllItems() {
        Map<CustomItemType, ItemLoader<?>> loaders = Map.of(
            CustomItemType.AMMO, YMLLModelLoader::loadAmmo,
            CustomItemType.ARMOR, YMLLModelLoader::loadArmor,
            CustomItemType.GRENADE, YMLLModelLoader::loadGrenade,
            CustomItemType.WEAPON, YMLLModelLoader::loadWeapon
        );

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
    private static void loadFolder(CustomItemType type, ThrowingFunction<YamlConfiguration, Boolean> configProcessor) {
        File folder = new File(plugin.getDataFolder(), type.getFolderPath());
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("Failed to create directory: " + folder.getPath());
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(YML_EXTENSION));
        if (files == null || files.length == 0) {
            plugin.getLogger().info("No " + type.getLabel() + " files found in " + folder.getPath());
            return;
        }

        int loadedCount = 0;
        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                if (!config.getBoolean("enabled", true)) continue;
                if (configProcessor.apply(config)) loadedCount++;
            } catch (ItemLoadException e) {
                plugin.getLogger().severe(e.getMessage() + "; In file: " + file.getName() + "; Skipping the item.");
            } catch (Exception e) {
                plugin.getLogger().severe("Error loading " + file.getName() + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("-Loaded " + loadedCount + " " + type.getLabel() + " types");
    }

    /**
     * Loads common base attributes shared by all custom items.<br>
     * Described by {@link CustomBase.BaseAttributes}
     *
     * @param config the YAML configuration to read from
     * @return the populated {@link CustomBase.BaseAttributes} object
     * @throws ItemLoadException if any required attribute is missing or failed validation
     */
    private static @NotNull BaseAttributes loadBaseAttributes(YamlConfiguration config)
        throws ItemLoadException {
        String name = config.getString("name");
        if (!CustomItemsRegistry.canNameBeUsed(name))
            throw new ItemLoadException("Name: '" + name + "' is already in use or is empty");

        String materialName = config.getString("material");
        int customModelData = config.getInt("customModelData");
        MaterialStorage materialStorage = MaterialStorage.create(materialName, customModelData);

        Component displayName = config.getRichMessage("displayName", ComponentUtils.noItalic(name, WHITE));

        List<Component> lore = config.getStringList("lore").stream()
            .map(line -> MiniMessage.miniMessage().deserialize(line))
            .collect(Collectors.toList());

        int maxStackSize = MathUtils.clamp(
            config.getInt("maxStackSize", materialStorage.material.getMaxStackSize()),
            1, 99
        );

        return new BaseAttributes(name, materialStorage, displayName, lore, maxStackSize);
    }

    /**
     * Loads an {@link Ammo} item from YAML config.
     *
     * @param config the YAML configuration
     * @return the constructed Ammo item
     * @throws ItemLoadException if validation fails
     */
    private static @NotNull Ammo loadAmmo(YamlConfiguration config) throws ItemLoadException {
        var baseAttributes = loadBaseAttributes(config);
        // Load only ammo-specific attributes

        return new Ammo(baseAttributes);
    }

    /**
     * Loads an {@link Armor} item from YAML config.
     *
     * @param config the YAML configuration
     * @return the constructed Armor item
     * @throws ItemLoadException if validation fails
     */
    private static @NotNull Armor loadArmor(YamlConfiguration config) throws ItemLoadException {
        var baseAttributes = loadBaseAttributes(config);
        // Load only armor-specific attributes

        return new Armor(baseAttributes);
    }

    /**
     * Loads a {@link Grenade} item from YAML config.
     *
     * @param config the YAML configuration
     * @return the constructed Grenade item
     * @throws ItemLoadException if validation fails
     */
    private static @NotNull Grenade loadGrenade(YamlConfiguration config) throws ItemLoadException {
        var baseAttributes = loadBaseAttributes(config);
        // Load only grenade-specific attributes

        return new Grenade(baseAttributes);
    }

    /**
     * Loads a {@link Weapon} item from YAML config.
     *
     * @param config the YAML configuration
     * @return the constructed Weapon item
     * @throws ItemLoadException if validation fails
     */
    private static @NotNull Weapon loadWeapon(YamlConfiguration config) throws ItemLoadException {
        var baseAttributes = loadBaseAttributes(config);
        // Load only weapon-specific attributes

        return new Weapon(baseAttributes);
    }
}