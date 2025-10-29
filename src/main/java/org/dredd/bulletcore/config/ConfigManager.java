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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.bukkit.SoundCategory.MASTER;
import static org.dredd.bulletcore.config.particles.ParticleManager.loadParticle;
import static org.dredd.bulletcore.config.sounds.SoundManager.loadSound;
import static org.dredd.bulletcore.config.sounds.SoundPlaybackMode.PLAYER;
import static org.dredd.bulletcore.config.sounds.SoundPlaybackMode.WORLD;

/**
 * Manages main plugin's configuration.
 * <p>
 * Loads values from the config file or defaults, creating a default file if missing.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class ConfigManager {

    // ----------< Static >----------

    //private static final String CONFIG_FILE_NAME = "config.yml";
    //private static final List<String> CONFIG_HEADER = List.of("Wiki: <link>");

    private static ConfigManager instance;

    public static ConfigManager instance() {
        return instance;
    }

    public static void load(@NotNull BulletCore plugin) {
        instance = new ConfigManager(plugin);
    }


    // ----------< Instance >----------

    // -----< Attributes >-----

    private final BulletCore plugin;

    public final Locale locale;

    public final boolean enableHotbarMessages;

    public final double raySize;

    public final long fireResumeThreshold;

    public final DamageThresholds damageThresholds;

    public final ConfiguredSound entityHitHeadSound;
    public final ConfiguredSound entityHitBodySound;
    public final ConfiguredSound blockHitSound;

    public final ConfiguredParticle entityHitParticle;
    public final ConfiguredParticle blockHitParticle;

    public final ASFeatureManager asFeatureManager;

    public final Set<Material> ignoredMaterials;

    // -----< Construction >-----

    private ConfigManager(@NotNull BulletCore plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        this.plugin = plugin;

        this.locale = Locale.forLanguageTag(cfg.getString("locale", "en-US"));

        this.enableHotbarMessages = cfg.getBoolean("enable-hotbar-messages", true);

        this.raySize = cfg.getDouble("ray-size", 0.1);

        this.fireResumeThreshold = Math.clamp(cfg.getLong("fire-resume-threshold", 1000L), -1L, Long.MAX_VALUE);

        this.damageThresholds = DamageThresholds.load(cfg);

        this.entityHitHeadSound = loadSound(cfg, "entity_hit_head", new ConfiguredSound("entity.experience_orb.pickup", MASTER, 0.5f, 1.0f, 0L, PLAYER));
        this.entityHitBodySound = loadSound(cfg, "entity_hit_body", new ConfiguredSound("block.beehive.drip", MASTER, 5.0f, 1.0f, 0L, WORLD));
        this.blockHitSound = loadSound(cfg, "block_hit", new ConfiguredSound("block.metal.hit", MASTER, 2.0f, 1.0f, 0L, WORLD));

        this.entityHitParticle = loadParticle(cfg, "entity_hit", new ConfiguredParticle(Particle.DAMAGE_INDICATOR, 1, null));
        this.blockHitParticle = loadParticle(cfg, "block_hit", new ConfiguredParticle(Particle.CRIT, 2, null));

        this.asFeatureManager = ASFeatureManager.load(cfg);

        this.ignoredMaterials = parseMaterials(cfg.getStringList("ignored-materials"));
    }

    // -----< Utilities >-----

    /**
     * Parses a list of material names into a set of {@link Material} instances.
     *
     * @param materialNames the list of material names to parse
     * @return a set of {@link Material} instances parsed from the given list of material names
     */
    private @NotNull Set<Material> parseMaterials(@NotNull List<String> materialNames) {
        final Set<Material> result = new HashSet<>();

        for (final String name : materialNames) {
            final Material material = Material.getMaterial(name.toUpperCase(Locale.ROOT));
            if (material != null)
                result.add(material);
            else
                plugin.logError("Skipping invalid ignored material \"" + name + "\"");
        }
        plugin.logInfo("-Loaded " + result.size() + " ignored materials");

        result.addAll(Materials.BLOCKS_NON_COLLIDABLE);

        // Adds 5 extra (PISTON_HEAD MOVING_PISTON WATER_CAULDRON LAVA_CAULDRON POWDER_SNOW_CAULDRON)
        // TODO: add configurable list of materials to filter out for each category
        result.addAll(Materials.BLOCKS_ONLY);

        plugin.logInfo("-Total " + result.size() + " ignored materials");

        return Collections.unmodifiableSet(result);
    }
}