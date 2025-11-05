package org.dredd.bulletcore.config;

import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.armorstand_features.ASFeatureManager;
import org.dredd.bulletcore.config.particles.ConfiguredParticle;
import org.dredd.bulletcore.config.particles.ParticleManager;
import org.dredd.bulletcore.config.sounds.ConfiguredSound;
import org.dredd.bulletcore.config.sounds.SoundManager;
import org.dredd.bulletcore.models.weapons.damage.DamageThresholds;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import static org.bukkit.SoundCategory.MASTER;
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

    // -----< Defaults >-----

    private static final ConfiguredSound DEFAULT_ENTITY_HIT_HEAD_SOUND = new ConfiguredSound(
        "entity.experience_orb.pickup", MASTER, 0.5f, 1.0f, 0L, PLAYER
    );

    private static final ConfiguredSound DEFAULT_ENTITY_HIT_BODY_SOUND = new ConfiguredSound(
        "block.beehive.drip", MASTER, 5.0f, 1.0f, 0L, WORLD
    );

    private static final ConfiguredSound DEFAULT_BLOCK_HIT_SOUND = new ConfiguredSound(
        "block.metal.hit", MASTER, 2.0f, 1.0f, 0L, WORLD
    );

    private static final ConfiguredParticle DEFAULT_ENTITY_HIT_PARTICLE = new ConfiguredParticle(
        Particle.DAMAGE_INDICATOR, 1, null
    );

    private static final ConfiguredParticle DEFAULT_BLOCK_HIT_PARTICLE = new ConfiguredParticle(
        Particle.CRIT, 2, null
    );

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

    // -----< Construction >-----

    private ConfigManager(@NotNull BulletCore plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        final FileConfiguration cfg = plugin.getConfig();

        this.plugin = plugin;

        this.locale = Locale.forLanguageTag(cfg.getString("locale", "en-US"));

        this.enableHotbarMessages = cfg.getBoolean("enable-hotbar-messages", true);

        this.raySize = cfg.getDouble("ray-size", 0.1);

        this.fireResumeThreshold = Math.clamp(cfg.getLong("fire-resume-threshold", 1000L), -1L, Long.MAX_VALUE);

        this.damageThresholds = DamageThresholds.load(cfg);

        this.entityHitHeadSound = SoundManager.loadSound(cfg, "entity-hit-head", DEFAULT_ENTITY_HIT_HEAD_SOUND);
        this.entityHitBodySound = SoundManager.loadSound(cfg, "entity-hit-body", DEFAULT_ENTITY_HIT_BODY_SOUND);
        this.blockHitSound = SoundManager.loadSound(cfg, "block-hit", DEFAULT_BLOCK_HIT_SOUND);

        this.entityHitParticle = ParticleManager.loadParticle(cfg, "entity-hit", DEFAULT_ENTITY_HIT_PARTICLE);
        this.blockHitParticle = ParticleManager.loadParticle(cfg, "block-hit", DEFAULT_BLOCK_HIT_PARTICLE);

        this.asFeatureManager = ASFeatureManager.load(cfg);
    }
}