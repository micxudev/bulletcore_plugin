package org.dredd.bulletcore.config.sounds;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.dredd.bulletcore.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * Utility class for loading and playing configured sounds defined in YAML.<br>
 * Sound configurations must be located under the {@code sounds.<key>} path in the YAML.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class SoundManager {

    /**
     * Private constructor to prevent instantiation.
     */
    private SoundManager() {}

    /**
     * Parses a {@link ConfiguredSound} from the given config using the key under {@code sounds.<key>}.
     *
     * @param cfg the YAML configuration to parse from
     * @param key the key under {@code sounds.<key>} to look up
     * @return a validated {@link ConfiguredSound} instance
     * @throws IllegalArgumentException if the configuration is missing or invalid
     */
    private static @NotNull ConfiguredSound parseSound(@NotNull FileConfiguration cfg, @NotNull String key) {
        String fullKey = "sounds." + key;
        ConfigurationSection section = cfg.getConfigurationSection(fullKey);
        if (section == null)
            throw new NoSuchElementException("Missing sound configuration for key: " + fullKey);

        String sound = section.getString("sound");
        if (sound == null || sound.isBlank())
            throw new IllegalArgumentException("Missing or empty 'sound' for key: " + fullKey);

        String categoryName = section.getString("category", "MASTER").toUpperCase(Locale.ROOT);
        SoundCategory category;
        try {
            category = SoundCategory.valueOf(categoryName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sound category '" + categoryName + "' for key: sounds." + key);
        }

        float volume = MathUtils.clamp((float) section.getDouble("volume", 1.0), 0.0f, Float.MAX_VALUE);

        float pitch = MathUtils.clamp((float) section.getDouble("pitch", 1.0), 0.5f, 2.0f);

        return new ConfiguredSound(sound, category, volume, pitch);
    }

    /**
     * Loads a {@link ConfiguredSound} from config, falling back to a default if parsing fails.
     *
     * @param cfg the YAML configuration
     * @param key the key under {@code sounds.<key>} to load
     * @param def the fallback {@link ConfiguredSound} to use if parsing fails
     * @return a valid {@link ConfiguredSound}, either parsed or fallback
     */
    public static @NotNull ConfiguredSound loadSound(@NotNull FileConfiguration cfg, @NotNull String key, @NotNull ConfiguredSound def) {
        try {
            return parseSound(cfg, key);
        } catch (NoSuchElementException ignored) {
            // Ignored, the sound configuration is optional
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().severe(e.getMessage() + "; Falling back to default sound");
        }
        return def;
    }

    /**
     * Plays the given {@link ConfiguredSound} at the specified location in the world.
     *
     * @param world    the world to play the sound in
     * @param location the location where the sound will be heard from
     * @param sound    the configured sound to play
     */
    public static void playSound(@NotNull World world, @NotNull Location location, @NotNull ConfiguredSound sound) {
        world.playSound(location, sound.sound(), sound.category(), sound.volume(), sound.pitch());
    }
}