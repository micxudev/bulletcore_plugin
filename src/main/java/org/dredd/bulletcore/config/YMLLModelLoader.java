package org.dredd.bulletcore.config;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.custom_item_manager.exceptions.ItemLoadException;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.models.CustomBase;
import org.dredd.bulletcore.models.CustomBase.BaseAttributes;
import org.dredd.bulletcore.models.CustomItemType;
import org.dredd.bulletcore.models.ammo.Ammo;
import org.dredd.bulletcore.models.armor.Armor;
import org.dredd.bulletcore.models.grenades.Grenade;
import org.dredd.bulletcore.models.weapons.BulletTrailParticle;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.models.weapons.WeaponSounds;
import org.dredd.bulletcore.models.weapons.damage.WeaponDamage;
import org.dredd.bulletcore.models.weapons.reloading.ReloadHandler;
import org.dredd.bulletcore.models.weapons.reloading.ReloadManager;
import org.dredd.bulletcore.models.weapons.shooting.recoil.WeaponRecoil;
import org.dredd.bulletcore.models.weapons.shooting.spray.WeaponSpray;
import org.dredd.bulletcore.models.weapons.skins.WeaponSkins;
import org.dredd.bulletcore.utils.ComponentUtils;
import org.dredd.bulletcore.utils.ThrowingFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;
import static org.dredd.bulletcore.config.messages.TranslatableMessages.*;
import static org.dredd.bulletcore.utils.ComponentUtils.MINI;
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
     * Reference to the plugin's main instance.
     */
    private static BulletCore plugin;

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
    public static void loadAllItems(BulletCore plugin) {
        YMLLModelLoader.plugin = plugin;

        Map<CustomItemType, ItemLoader<?>> loaders = new LinkedHashMap<>() {{
            put(CustomItemType.AMMO, YMLLModelLoader::loadAmmo);
            put(CustomItemType.ARMOR, YMLLModelLoader::loadArmor);
            put(CustomItemType.GRENADE, YMLLModelLoader::loadGrenade);
            put(CustomItemType.WEAPON, YMLLModelLoader::loadWeapon);
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
    private static void loadFolder(CustomItemType type, ThrowingFunction<YamlConfiguration, Boolean> configProcessor) {
        File folder = new File(plugin.getDataFolder(), type.getFolderPath());
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("Failed to create directory: " + folder.getPath());
            return;
        }

        File[] files = folder.listFiles(
            (dir, name) -> {
                String lower = name.toLowerCase(Locale.ROOT);
                return lower.endsWith(".yml") || lower.endsWith(".yaml");
            });
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
    private static @NotNull BaseAttributes loadBaseAttributes(YamlConfiguration config) throws ItemLoadException {
        String name = config.getString("name");
        if (!CustomItemsRegistry.canNameBeUsed(name))
            throw new ItemLoadException("Name: '" + name + "' does not match [a-z0-9/._-] or is already in use");

        int customModelData = config.getInt("customModelData");
        if (!CustomItemsRegistry.canModelDataBeUsed(customModelData))
            throw new ItemLoadException("CustomModelData: " + customModelData + " is already in use or does not end with 2 zeroes");

        Material material = getMetaCapableMaterial(config.getString("material"));

        Component displayName = config.getRichMessage("displayName", ComponentUtils.noItalic(name, WHITE));

        List<Component> lore = config.getStringList("lore").stream()
            .map(MINI::deserialize)
            .collect(Collectors.toList());

        int maxStackSize = Math.clamp(config.getInt("maxStackSize", material.getMaxStackSize()), 1, 99);

        return new BaseAttributes(name, customModelData, material, displayName, lore, maxStackSize);
    }

    /**
     * Parses the given material name and returns the Material if it supports {@link ItemMeta}.
     *
     * @param materialName The name of the Material (e.g., "DIAMOND_SWORD")
     * @return Material that supports ItemMeta
     * @throws ItemLoadException if the material is invalid or does not support ItemMeta
     */
    private static @NotNull Material getMetaCapableMaterial(@Nullable String materialName)
        throws ItemLoadException {
        if (materialName == null || materialName.isBlank())
            throw new ItemLoadException("Material name cannot be null or blank");

        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            throw new ItemLoadException("Invalid material name: " + materialName);
        }

        if (!material.isItem())
            throw new ItemLoadException("Material is not an item: " + material);

        if (new ItemStack(material).getItemMeta() == null)
            throw new ItemLoadException("Material does not support ItemMeta: " + material);

        return material;
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

        int maxAmmo = Math.clamp(config.getInt("maxAmmo", 100), 1, Integer.MAX_VALUE);

        var lore = baseAttributes.lore();
        lore.add(0, text("Ammo count will be here on ItemStack creation", WHITE));

        return new Ammo(baseAttributes, maxAmmo);
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

        double maxDurability = Math.clamp(config.getDouble("maxDurability", 100.0D), 1.0D, Double.MAX_VALUE);
        double damageReduction = Math.clamp(config.getDouble("damageReduction", 0.5D), 0.0D, 1.0D);
        boolean unbreakable = config.getBoolean("unbreakable", true);

        int armorPoints = Math.clamp(config.getInt("armorPoints", 0), 0, 30);
        int toughnessPoints = Math.clamp(config.getInt("toughnessPoints", 0), 0, 20);

        double knockbackResistance = Math.clamp(config.getDouble("knockbackResistance", 0.0D), 0.0D, 1.0D);
        double explosionKnockbackResistance = Math.clamp(config.getDouble("explosionKnockbackResistance", 0.0D), 0.0D, 1.0D);

        var lore = baseAttributes.lore();
        lore.add(0, text("Durability will be here on ItemStack creation", WHITE));
        lore.add(1, LORE_ARMOR_DAMAGE_REDUCTION.of((int) (damageReduction * 100)));
        lore.add(2, LORE_ARMOR_ARMOR_POINTS.of(armorPoints));
        lore.add(3, LORE_ARMOR_TOUGHNESS_POINTS.of(toughnessPoints));
        lore.add(4, LORE_ARMOR_KNOCKBACK_RESISTANCE.of((int) (knockbackResistance * 100)));
        lore.add(5, LORE_ARMOR_EXPLOSION_KNOCKBACK_RESISTANCE.of((int) (explosionKnockbackResistance * 100)));

        return new Armor(baseAttributes, maxDurability, damageReduction, unbreakable, armorPoints, toughnessPoints, knockbackResistance, explosionKnockbackResistance);
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

        String ammoName = config.getString("ammo", "");
        Ammo ammo = CustomItemsRegistry.ammo.getItemOrNull(ammoName);
        if (ammo == null)
            throw new ItemLoadException("Invalid ammo name: " + ammoName);

        String reloadHandlerName = config.getString("reloadHandler", "default");
        ReloadHandler reloadHandler = ReloadManager.getHandlerOrNull(reloadHandlerName);
        if (reloadHandler == null)
            throw new ItemLoadException("Invalid reload handler name: " + reloadHandlerName);

        double maxDistance = Math.clamp(config.getDouble("maxDistance", 64), 1.0D, 300.0D);
        long delayBetweenShots = Math.clamp(config.getLong("delayBetweenShots", 500L), 50L, Long.MAX_VALUE);
        int maxBullets = Math.clamp(config.getInt("maxBullets", 10), 1, Integer.MAX_VALUE);
        long reloadTime = Math.clamp(config.getLong("reloadTime", 3000L), 100L, Long.MAX_VALUE);
        boolean isAutomatic = config.getBoolean("isAutomatic", false);
        double victimKnockbackResistance = Math.clamp(config.getDouble("victimKnockbackResistance", 0.0D), 0.0D, 1.0D);
        int pelletsPerShot = Math.clamp(config.getInt("pelletsPerShot", 1), 1, 20);

        WeaponDamage damage = WeaponDamage.load(config);
        WeaponRecoil recoil = WeaponRecoil.load(config);
        WeaponSpray spray = WeaponSpray.load(config);
        WeaponSounds sounds = WeaponSounds.load(config);
        BulletTrailParticle trailParticle = BulletTrailParticle.load(config);
        WeaponSkins skins = WeaponSkins.load(config, baseAttributes.customModelData(), baseAttributes.displayName());

        var lore = baseAttributes.lore();
        lore.add(0, text("Bullets will be here on ItemStack creation", WHITE));
        lore.add(1, LORE_WEAPON_DAMAGE.of(damage.head(), damage.body(), damage.legs(), damage.feet()));
        lore.add(2, LORE_WEAPON_DISTANCE.of(maxDistance));
        lore.add(3, LORE_WEAPON_AMMO.of(ammo.displayNameString));

        return new Weapon(baseAttributes, ammo, reloadHandler, maxDistance, delayBetweenShots, maxBullets, reloadTime, isAutomatic, victimKnockbackResistance, pelletsPerShot, damage, recoil, spray, sounds, trailParticle, skins);
    }
}