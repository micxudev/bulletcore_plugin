package org.dredd.bulletcore.config;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.armorstand_features.ASFeatureManager;
import org.dredd.bulletcore.config.particles.ConfiguredParticle;
import org.dredd.bulletcore.config.sounds.ConfiguredSound;
import org.dredd.bulletcore.models.weapons.damage.DamageThresholds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

import static org.bukkit.SoundCategory.MASTER;
import static org.dredd.bulletcore.config.particles.ParticleManager.loadParticle;
import static org.dredd.bulletcore.config.sounds.SoundManager.loadSound;
import static org.dredd.bulletcore.config.sounds.SoundPlaybackMode.PLAYER;
import static org.dredd.bulletcore.config.sounds.SoundPlaybackMode.WORLD;

/**
 * Class for loading and managing the plugin's configuration.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class ConfigManager {

    /**
     * The singleton instance of the {@link ConfigManager}.
     */
    private static ConfigManager instance;

    /**
     * Gets the singleton instance of the {@link ConfigManager}
     *
     * @return the singleton instance, or {@code null} if called before {@link #reload(BulletCore)}
     */
    public static ConfigManager get() {
        return instance;
    }

    /**
     * Initializes or reloads the config.
     *
     * @param plugin the plugin instance
     */
    public static void reload(BulletCore plugin) {
        instance = new ConfigManager(plugin);
    }

    public final Locale locale;

    public final double bulletTrailStep;
    public final double startTrailOffset;

    public final boolean enableHotbarMessages;

    public final double raySize;

    public final DamageThresholds damageThresholds;

    public final ConfiguredSound entityHitHeadSound;
    public final ConfiguredSound entityHitBodySound;
    public final ConfiguredSound blockHitSound;

    public final ConfiguredParticle entityHitParticle;
    public final ConfiguredParticle blockHitParticle;
    public final ConfiguredParticle bulletTrailParticle;

    public final ASFeatureManager asFeatureManager;

    public final @Unmodifiable Set<Material> ignoredMaterials;

    /**
     * Initializes the {@link ConfigManager} instance by loading and parsing configuration values
     * from the plugin's {@code config.yml} file. This constructor ensures that default
     * configuration values are saved and reloaded before using them.
     *
     * @param plugin the {@link BulletCore} instance
     */
    private ConfigManager(BulletCore plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        locale = Locale.forLanguageTag(cfg.getString("locale", "en-US"));

        bulletTrailStep = cfg.getDouble("bullet-trail-step", 1.0);
        startTrailOffset = Math.clamp(cfg.getDouble("start-trail-offset", 2.0), 0.0, 50.0);

        enableHotbarMessages = cfg.getBoolean("enable-hotbar-messages", true);

        raySize = cfg.getDouble("ray-size", 0.01);
        damageThresholds = DamageThresholds.load(cfg);

        entityHitHeadSound = loadSound(cfg, "entity_hit_head", new ConfiguredSound("entity.experience_orb.pickup", MASTER, 0.5f, 1.0f, 0L, PLAYER));
        entityHitBodySound = loadSound(cfg, "entity_hit_body", new ConfiguredSound("block.beehive.drip", MASTER, 5.0f, 1.0f, 0L, WORLD));
        blockHitSound = loadSound(cfg, "block_hit", new ConfiguredSound("block.metal.hit", MASTER, 2.0f, 1.0f, 0L, WORLD));

        entityHitParticle = loadParticle(cfg, "entity_hit", new ConfiguredParticle(Particle.DAMAGE_INDICATOR, 4, null));
        blockHitParticle = loadParticle(cfg, "block_hit", new ConfiguredParticle(Particle.CRIT, 4, null));
        bulletTrailParticle = loadParticle(cfg, "bullet_trail", new ConfiguredParticle(Particle.ASH, 1, null));

        asFeatureManager = new ASFeatureManager(cfg.getConfigurationSection("armorstand-features"));

        ignoredMaterials = parseMaterials(cfg.getStringList("ignored-materials"));
        plugin.getLogger().info("-Loaded " + ignoredMaterials.size() + " ignored materials");
    }

    /**
     * Parses a list of material names into a set of {@link Material} instances.
     *
     * @param materialNames the list of material names to parse; must not be null
     * @return a set of {@link Material} instances parsed from the given list of material names
     */
    public static @NotNull @Unmodifiable Set<Material> parseMaterials(@NotNull List<String> materialNames) {
        Set<Material> parsedMaterials = HashSet.newHashSet(materialNames.size());

        for (String name : materialNames) {
            try {
                Material material = Material.valueOf(name.toUpperCase(Locale.ROOT));
                parsedMaterials.add(material);
            } catch (IllegalArgumentException e) {
                BulletCore.getInstance().getLogger().warning("Skipping invalid material in ignored-materials: " + name);
            }
        }

        return Collections.unmodifiableSet(parsedMaterials);
    }
}