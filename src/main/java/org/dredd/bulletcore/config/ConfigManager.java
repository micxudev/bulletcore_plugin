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
    private static final String CONFIG_FILE_NAME = "config.yml";
    private static final List<String> CONFIG_HEADER = List.of("Wiki: <link>");

    private static ConfigManager instance;

    public static ConfigManager instance() {
        return instance;
    }

    public static void load(@NotNull BulletCore plugin) {
        instance = new ConfigManager(plugin);
    }

    // ----------< Instance >----------
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

    private ConfigManager(@NotNull BulletCore plugin) {
        this.plugin = plugin;

        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        locale = Locale.forLanguageTag(cfg.getString("locale", "en-US"));

        enableHotbarMessages = cfg.getBoolean("enable-hotbar-messages", true);

        raySize = cfg.getDouble("ray-size", 0.01);

        fireResumeThreshold = Math.clamp(cfg.getLong("fire-resume-threshold", 1000L), -1L, Long.MAX_VALUE);

        damageThresholds = DamageThresholds.load(cfg);

        entityHitHeadSound = loadSound(cfg, "entity_hit_head", new ConfiguredSound("entity.experience_orb.pickup", MASTER, 0.5f, 1.0f, 0L, PLAYER));
        entityHitBodySound = loadSound(cfg, "entity_hit_body", new ConfiguredSound("block.beehive.drip", MASTER, 5.0f, 1.0f, 0L, WORLD));
        blockHitSound = loadSound(cfg, "block_hit", new ConfiguredSound("block.metal.hit", MASTER, 2.0f, 1.0f, 0L, WORLD));

        entityHitParticle = loadParticle(cfg, "entity_hit", new ConfiguredParticle(Particle.DAMAGE_INDICATOR, 1, null));
        blockHitParticle = loadParticle(cfg, "block_hit", new ConfiguredParticle(Particle.CRIT, 2, null));

        asFeatureManager = ASFeatureManager.load(cfg);

        ignoredMaterials = parseMaterials(cfg.getStringList("ignored-materials"));
        plugin.getLogger().info("-Loaded " + ignoredMaterials.size() + " ignored materials");
    }

    /**
     * Parses a list of material names into a set of {@link Material} instances.
     *
     * @param materialNames the list of material names to parse
     * @return a set of {@link Material} instances parsed from the given list of material names
     */
    private @NotNull Set<Material> parseMaterials(@NotNull List<String> materialNames) {
        Set<Material> result = HashSet.newHashSet(materialNames.size());

        for (String name : materialNames) {
            Material material = Material.getMaterial(name.toUpperCase(Locale.ROOT));
            if (material != null)
                result.add(material);
            else
                plugin.getLogger().severe("Skipping invalid material in ignored-materials: " + name);
        }
        return Collections.unmodifiableSet(result);
    }
}