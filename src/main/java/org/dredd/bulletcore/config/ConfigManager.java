package org.dredd.bulletcore.config;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * Class for loading and managing the plugin's configuration.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class ConfigManager {

    /**
     * Singleton instance of the {@link ConfigManager}.
     */
    private static ConfigManager config;

    /**
     * Returns the singleton instance of {@link ConfigManager} or reloads config it if it doesn't exist.
     *
     * @param plugin the plugin instance
     * @return the singleton instance
     */
    public static ConfigManager getOrLoad(BulletCore plugin) {
        if (config == null) config = new ConfigManager(plugin);
        return config;
    }

    /**
     * Reloads the singleton instance of {@link ConfigManager}.
     *
     * @param plugin the plugin instance
     */
    public static void reload(BulletCore plugin) {
        config = new ConfigManager(plugin);
    }

    public final boolean enableFriendlyFire;
    public final double bulletTrailStep;
    public final boolean enableMuzzleFlashes;
    public final boolean enableRecoil;

    public final boolean enableHotbarOutOfAmmo;
    public final boolean enableHotbarShoot;
    public final boolean enableHotbarReload;
    public final boolean enableLoreGunInfoMessages;

    public final @Unmodifiable Set<Material> ignoredMaterials;

    /**
     * Initializes the {@code ConfigManager} by loading and parsing configuration values
     * from the plugin's {@code config.yml} file. This constructor ensures that default
     * configuration values are saved and reloaded before using them.
     *
     * @param plugin the main plugin instance used to access the configuration API
     */
    private ConfigManager(BulletCore plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        enableFriendlyFire = cfg.getBoolean("enable-friendly-fire", false);
        bulletTrailStep = cfg.getDouble("bullet-trail-step", 1.0);
        enableMuzzleFlashes = cfg.getBoolean("enable-muzzle-flashes", true);
        enableRecoil = cfg.getBoolean("enable-recoil", true);

        enableHotbarOutOfAmmo = cfg.getBoolean("enable-hotbar-messages.out-of-ammo", true);
        enableHotbarShoot = cfg.getBoolean("enable-hotbar-messages.shoot", true);
        enableHotbarReload = cfg.getBoolean("enable-hotbar-messages.reload", true);
        enableLoreGunInfoMessages = cfg.getBoolean("enable-lore-gun-info-messages", true);

        ignoredMaterials = parseMaterials(cfg.getStringList("ignored-materials"));
        plugin.getLogger().info("-Loaded " + ignoredMaterials.size() + " ignored materials");
    }

    public static @NotNull @Unmodifiable Set<Material> parseMaterials(@NotNull List<String> materialNames) {
        Set<Material> ignoredMaterials = HashSet.newHashSet(materialNames.size());

        for (String name : materialNames) {
            try {
                Material material = Material.valueOf(name.toUpperCase(Locale.ROOT));
                ignoredMaterials.add(material);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Skipping invalid material in ignored-materials: " + name);
            }
        }

        return Collections.unmodifiableSet(ignoredMaterials);
    }
}