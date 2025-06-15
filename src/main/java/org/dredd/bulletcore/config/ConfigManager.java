package org.dredd.bulletcore.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.utils.MathUtils;

public final class ConfigManager {

    /**
     * Singleton instance of the {@link ConfigManager}.
     */
    private volatile static ConfigManager config;

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

    // Config values
    public final String language;

    public final boolean friendlyFireEnabled;
    public final double bulletDetectionStep;
    public final double bulletTrailStep;
    public final boolean enableRecoil;
    public final String headshotSound;

    public final boolean enableHotbarOutOfAmmo;
    public final boolean enableHotbarShoot;
    public final boolean enableHotbarReload;

    public final boolean enableLoreGunInfoMessages;

    public final boolean blockBulletsDoor;
    public final boolean blockBulletsLeaves;
    public final boolean blockBulletsWater;
    public final boolean blockBulletsGlass;

    public final boolean enableExplosions;
    public final boolean enableMuzzleFlashes;

    private ConfigManager(BulletCore plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        language = cfg.getString("language", "en");
        friendlyFireEnabled = cfg.getBoolean("friendly-fire-enabled", false);
        bulletDetectionStep = MathUtils.clamp(cfg.getDouble("bullet-detection-step", 0.1), 0.05, 1.0);
        bulletTrailStep = cfg.getDouble("bullet-trail-step", 1.0);
        enableRecoil = cfg.getBoolean("enable-recoil", true);
        headshotSound = cfg.getString("headshot-sound", "entity.experience_orb.pickup");

        enableHotbarOutOfAmmo = cfg.getBoolean("enable-hotbar-messages.out-of-ammo", true);
        enableHotbarShoot = cfg.getBoolean("enable-hotbar-messages.shoot", true);
        enableHotbarReload = cfg.getBoolean("enable-hotbar-messages.reload", true);

        enableLoreGunInfoMessages = cfg.getBoolean("enable-lore-gun-info-messages", true);

        blockBulletsDoor = cfg.getBoolean("block-bullets.door", false);
        blockBulletsLeaves = cfg.getBoolean("block-bullets.leaves", false);
        blockBulletsWater = cfg.getBoolean("block-bullets.water", false);
        blockBulletsGlass = cfg.getBoolean("block-bullets.glass", false);

        enableExplosions = cfg.getBoolean("enable-explosions", false);
        enableMuzzleFlashes = cfg.getBoolean("enable-muzzle-flashes", false);
    }
}