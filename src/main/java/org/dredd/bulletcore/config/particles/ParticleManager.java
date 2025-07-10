package org.dredd.bulletcore.config.particles;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * Utility class for loading and spawning configured particles defined in YAML.<br>
 * Particle configurations must be located under the {@code particles.<key>} path in the YAML.
 *
 * @author dredd
 * @since 1.0.0
 */
public class ParticleManager {

    /**
     * Private constructor to prevent instantiation.
     */
    private ParticleManager() {}

    /**
     * Parses a {@link ConfiguredParticle} from the given config using the key under {@code particles.<key>}.
     *
     * @param cfg the YAML configuration to parse from
     * @param key the key under {@code particles.<key>} to look up
     * @return a validated {@link ConfiguredParticle} instance
     * @throws NoSuchElementException   if the configuration is missing
     * @throws IllegalArgumentException if the configuration is invalid
     */
    private static @NotNull ConfiguredParticle parseParticle(@NotNull FileConfiguration cfg, @NotNull String key) {
        String fullKey = "particles." + key;
        ConfigurationSection section = cfg.getConfigurationSection(fullKey);
        if (section == null)
            throw new NoSuchElementException("Missing particle configuration for key: " + fullKey);

        String particleName = section.getString("particle");
        if (particleName == null || particleName.isBlank())
            throw new IllegalArgumentException("Missing or empty 'particle' for key: " + fullKey);

        Particle particle;
        try {
            particle = Particle.valueOf(particleName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid particle '" + particleName + "' for key: " + fullKey);
        }

        int count = MathUtils.clamp(section.getInt("count", 1), 0, Integer.MAX_VALUE);

        return new ConfiguredParticle(particle, count);
    }

    /**
     * Loads a {@link ConfiguredParticle} from config, falling back to a default if parsing fails.
     *
     * @param cfg the YAML configuration
     * @param key the key under {@code particles.<key>} to load
     * @param def the fallback {@link ConfiguredParticle} to use if parsing fails
     * @return a valid {@link ConfiguredParticle}, either parsed or fallback
     */
    public static @NotNull ConfiguredParticle loadParticle(@NotNull FileConfiguration cfg, @NotNull String key, @NotNull ConfiguredParticle def) {
        try {
            return parseParticle(cfg, key);
        } catch (NoSuchElementException ignored) {
            // Ignored, the particle configuration is optional
        } catch (IllegalArgumentException e) {
            BulletCore.getInstance().getLogger().severe(e.getMessage() + "; Falling back to default particle");
        }
        return def;
    }

    /**
     * Spawns the given {@link ConfiguredParticle} at the specified location in the world.
     *
     * @param world    the world to spawn the particle in
     * @param location the location where the particle should appear
     * @param particle the configured particle to spawn
     */
    public static void spawnParticle(@NotNull World world, @NotNull Location location, @NotNull ConfiguredParticle particle) {
        world.spawnParticle(particle.particle(), location, particle.count());
    }
}